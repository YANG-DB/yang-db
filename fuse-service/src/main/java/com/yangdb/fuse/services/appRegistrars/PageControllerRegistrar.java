package com.yangdb.fuse.services.appRegistrars;

/*-
 * #%L
 * fuse-service
 * %%
 * Copyright (C) 2016 - 2019 The YangDb Graph Database Project
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

/*-
 *
 * fuse-service
 * %%
 * Copyright (C) 2016 - 2019 yangdb   ------ www.yangdb.org ------
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

import com.yangdb.fuse.dispatcher.urlSupplier.AppUrlSupplier;
import com.yangdb.fuse.logging.Route;
import com.yangdb.fuse.model.execution.plan.PlanWithCost;
import com.yangdb.fuse.model.execution.plan.composite.Plan;
import com.yangdb.fuse.model.execution.plan.costs.PlanDetailedCost;
import com.yangdb.fuse.model.execution.plan.descriptors.PlanWithCostDescriptor;
import com.yangdb.fuse.model.resourceInfo.PageResourceInfo;
import com.yangdb.fuse.model.results.AssignmentDescriptor;
import com.yangdb.fuse.model.results.AssignmentsQueryResult;
import com.yangdb.fuse.model.transport.ContentResponse;
import com.yangdb.fuse.model.transport.CreatePageRequest;
import com.yangdb.fuse.model.transport.cursor.CreateGraphCursorRequest;
import com.yangdb.fuse.rendering.SVGGraphRenderer;
import com.yangdb.fuse.services.controllers.PageController;
import org.jooby.Jooby;
import org.jooby.Request;
import org.jooby.Response;
import org.jooby.Results;

import java.io.File;

import static org.jooby.Status.NOT_FOUND;
import static org.jooby.Status.OK;

public class PageControllerRegistrar extends AppControllerRegistrarBase<PageController> {
    //region Constructors
    public PageControllerRegistrar() {
        super(PageController.class);
    }
    //endregion

    //region AppControllerRegistrarBase Implementation
    @Override
    public void register(Jooby app, AppUrlSupplier appUrlSupplier) {
        /** get the cursor page store info*/
        app.get(appUrlSupplier.pageStoreUrl(":queryId", ":cursorId"),
                req -> {
                    Route.of("getPageStore").write();

                    ContentResponse response = this.getController(app).getInfo(req.param("queryId").value(), req.param("cursorId").value());
                    return Results.with(response, response.status());
                });

        /** create the next page */
        app.post(appUrlSupplier.pageStoreUrl(":queryId", ":cursorId"),
                req -> {
                    Route.of("postPage").write();
                    CreatePageRequest createPageRequest = req.body(CreatePageRequest.class);
                    req.set(CreatePageRequest.class, createPageRequest);
                    ContentResponse<PageResourceInfo> entity = createPageRequest.isFetch() ?
                            this.getController(app).createAndFetch(req.param("queryId").value(), req.param("cursorId").value(), createPageRequest) :
                            this.getController(app).create(req.param("queryId").value(), req.param("cursorId").value(), createPageRequest);
                    return Results.with(entity, entity.status());
                });

        /** view the elastic query with d3 html*/
        app.get(appUrlSupplier.resourceUrl(":queryId", ":cursorId", ":pageId") + "/elastic/view",
                req -> Results.redirect("/public/assets/ElasticQueryViewer.html?q=" +
                        appUrlSupplier.pageStoreUrl(req.param("queryId").value(), req.param("cursorId").value()) + "/" + req.param("pageId").value() + "/elastic"));


        /** get page info by id */
        app.get(appUrlSupplier.resourceUrl(":queryId", ":cursorId", ":pageId"),
                req -> {
                    Route.of("getPage").write();
                    ContentResponse response = this.getController(app).getInfo(req.param("queryId").value(), req.param("cursorId").value(), req.param("pageId").value());
                    return Results.with(response, response.status());
                });

        /** get page data by id */
        app.get(appUrlSupplier.resourceUrl(":queryId", ":cursorId", ":pageId") + "/data/visualize",
                (req,resp) -> API.dataView(app,req,resp,this));



        app.get(appUrlSupplier.resourceUrl(":queryId", ":cursorId", ":pageId") + "/data",
                req -> {
                    Route.of("getPageData").write();

                    ContentResponse response = this.getController(app).getData(req.param("queryId").value(), req.param("cursorId").value(), req.param("pageId").value());
                    return Results.with(response, response.status());
                });

        /** get page data by format */
        app.get(appUrlSupplier.resourceUrl(":queryId", ":cursorId", ":pageId", ":format") + "/data",
                req -> {
                    Route.of("getDataWithFormat").write();
                    CreateGraphCursorRequest.GraphFormat format = CreateGraphCursorRequest.GraphFormat.valueOf(req.param("format").value());
                    ContentResponse response = this.getController(app).format(req.param("queryId").value(), req.param("cursorId").value(), req.param("pageId").value(), format);
                    switch (format) {
                        case JSON:
                            return Results.with(response);

                        case XML:
                            return Results.xml(response.content());

                        default:
                            return Results.with(response);
                    }
                });

        /** get page data by id */
        app.delete(appUrlSupplier.resourceUrl(":queryId", ":cursorId", ":pageId"),
                req -> {
                    Route.of("deletePage").write();

                    ContentResponse response = this.getController(app).delete(req.param("queryId").value(), req.param("cursorId").value(), req.param("pageId").value());
                    return Results.with(response, response.status());
                });
    }

    public static class API {
        /**
         * print an SVG representation of the data
         * @param app
         * @param req
         * @param resp
         * @param controller
         * @return
         * @throws Throwable
         */
        public static Object dataView(Jooby app, Request req, Response resp, PageControllerRegistrar controller) throws Throwable {

            Route.of("getPageData").write();

            String queryId = req.param("queryId").value();
            ContentResponse response = controller.getController(app).getData(queryId, req.param("cursorId").value(), req.param("pageId").value());
            AssignmentsQueryResult data = (AssignmentsQueryResult) response.getData();
            String dotGraph = AssignmentDescriptor.printGraph(data.getAssignments());
            File file = SVGGraphRenderer.render(queryId, dotGraph);
            ContentResponse<File> compose = ContentResponse.Builder.<File>builder(OK, NOT_FOUND)
                    .data(file)
                    .compose();
            return RegistrarsUtils.withImg(req,resp,compose);
        }


    }
        //endregion
}
