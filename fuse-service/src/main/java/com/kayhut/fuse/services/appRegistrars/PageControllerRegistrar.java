package com.kayhut.fuse.services.appRegistrars;

import com.fasterxml.jackson.databind.JsonNode;
import com.kayhut.fuse.dispatcher.urlSupplier.AppUrlSupplier;
import com.kayhut.fuse.logging.Route;
import com.kayhut.fuse.model.resourceInfo.PageResourceInfo;
import com.kayhut.fuse.model.transport.ContentResponse;
import com.kayhut.fuse.model.transport.CreatePageRequest;
import com.kayhut.fuse.services.controllers.PageController;
import org.jooby.Jooby;
import org.jooby.Results;

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
        app.use(appUrlSupplier.pageStoreUrl(":queryId", ":cursorId"))
                .get(req -> {
                    Route.of("getPageStore").write();

                    ContentResponse response = this.getController(app).getInfo(req.param("queryId").value(), req.param("cursorId").value());
                    return Results.with(response, response.status());
                });

        /** create the next page */
        app.use(appUrlSupplier.pageStoreUrl(":queryId", ":cursorId"))
                .post(req -> {
                    Route.of("postPage").write();

                    CreatePageRequest createPageRequest = req.body(CreatePageRequest.class);
                    req.set(CreatePageRequest.class, createPageRequest);
                    ContentResponse<PageResourceInfo> entity = createPageRequest.isFetch() ?
                            this.getController(app).createAndFetch(req.param("queryId").value(), req.param("cursorId").value(), createPageRequest) :
                            this.getController(app).create(req.param("queryId").value(), req.param("cursorId").value(), createPageRequest);
                    return Results.with(entity, entity.status());
                });

        /** view the elastic query with d3 html*/
        app.use(appUrlSupplier.resourceUrl(":queryId", ":cursorId", ":pageId") + "/elastic/view")
                .get(req -> Results.redirect("/public/assets/ElasticQueryViewer.html?q=" +
                        appUrlSupplier.pageStoreUrl(req.param("queryId").value(), req.param("cursorId").value()) + "/" + req.param("pageId").value() + "/elastic"));


        /** get page info by id */
        app.use(appUrlSupplier.resourceUrl(":queryId", ":cursorId", ":pageId"))
                .get(req -> {
                    Route.of("getPage").write();

                    ContentResponse response = this.getController(app).getInfo(req.param("queryId").value(), req.param("cursorId").value(), req.param("pageId").value());
                    return Results.with(response, response.status());
                });

        /** get page data by id */
        app.use(appUrlSupplier.resourceUrl(":queryId", ":cursorId", ":pageId") + "/data")
                .get(req -> {
                    Route.of("getPageData").write();

                    ContentResponse response = this.getController(app).getData(req.param("queryId").value(), req.param("cursorId").value(), req.param("pageId").value());
                    return Results.with(response, response.status());
                });

        /** get page data by id */
        app.use(appUrlSupplier.resourceUrl(":queryId", ":cursorId", ":pageId"))
                .delete(req -> {
                    Route.of("deletePage").write();

                    ContentResponse response = this.getController(app).delete(req.param("queryId").value(), req.param("cursorId").value(), req.param("pageId").value());
                    return Results.with(response, response.status());
                });
    }
    //endregion
}
