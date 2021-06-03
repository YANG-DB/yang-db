package com.yangdb.dragons.services;

/*-
 * #%L
 * fuse-domain-dragons-ext
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

import com.yangdb.fuse.dispatcher.urlSupplier.AppUrlSupplier;
import com.yangdb.fuse.logging.Route;
import com.yangdb.fuse.model.resourceInfo.QueryResourceInfo;
import com.yangdb.fuse.model.transport.ContentResponse;
import com.yangdb.fuse.model.transport.CreateJsonQueryRequest;
import com.yangdb.fuse.model.transport.ExecutionScope;
import com.yangdb.fuse.model.transport.PlanTraceOptions;
import com.yangdb.fuse.services.appRegistrars.AppControllerRegistrarBase;
import com.yangdb.fuse.services.appRegistrars.RegistrarsUtils;
import com.yangdb.fuse.services.controllers.QueryController;
import org.jooby.*;
import org.json.JSONObject;

import java.util.Collections;
import java.util.Map;

import static com.yangdb.fuse.services.appRegistrars.QueryControllerRegistrar.API.runCypher;
import static com.yangdb.fuse.services.appRegistrars.QueryControllerRegistrar.API.runV1;


public class DragonsExtensionRegistrar extends AppControllerRegistrarBase<DragonsExtensionQueryController> {

    //region Constructors
    public DragonsExtensionRegistrar() {
        super(DragonsExtensionQueryController.class);
    }
    //endregion

    //region AppControllerRegistrarBase Implementation
    @Override
    public void register(Jooby app, AppUrlSupplier appUrlSupplier) {
        app.get("ext", () -> Results.redirect("/public/assets/swagger/swagger-ext.json"));

        /** create a clause query */
        app.post(appUrlSupplier.queryStoreUrl() + "/clause",req -> postClause(app,req,this.getController(app)));
        /** run a clause query */
        app.post(appUrlSupplier.queryStoreUrl() + "/clause/run",( req,res) -> runClause(app,req,res,this.getController(app)));
        /** run a cypher query */
        app.get(appUrlSupplier.queryStoreUrl() + "/cypher/logical/run",( req,res) -> runCypher(app,req, res,this.getController(app)));
        /** run a v1 query */
        app.post(appUrlSupplier.queryStoreUrl() + "/query/v1/logical/run",( req,res)-> runV1(app,req,res, this.getController(app)));

    }

    public static Result postClause(Jooby app, final Request req, QueryController controller) throws Exception {
        Route.of("postClause").write();

        Map<String,Object> createQueryRequest = req.body(Map.class);
        String query = new JSONObject((Map) createQueryRequest.get("query")).toString();
        CreateJsonQueryRequest request = new CreateJsonQueryRequest(
                createQueryRequest.get("id").toString(),
                createQueryRequest.get("name").toString(),
                createQueryRequest.get("queryType").toString(),
                query,
                createQueryRequest.get("ontology").toString());

        req.set(CreateJsonQueryRequest.class, request);
        req.set(PlanTraceOptions.class, request.getPlanTraceOptions());
        final long maxExecTime = request.getCreateCursorRequest() != null
                ? request.getCreateCursorRequest().getMaxExecutionTime() : 0;
        req.set(ExecutionScope.class, new ExecutionScope(Math.max(maxExecTime, 1000 * 60 * 10)));

        ContentResponse<QueryResourceInfo> response = request.getCreateCursorRequest() == null ?
                controller.create(request) :
                controller.createAndFetch(request);

        return Results.with(response, response.status());

    }


    public static void runClause(Jooby app, final Request req, final Response resp, QueryController controller) throws Throwable {
        Route.of("runClause").write();

        Map<String,Object> createQueryRequest = req.body(Map.class);
        String query = new JSONObject(Collections.singletonMap("clause",createQueryRequest.get("clause"))).toString();
        String ontology = req.param("ontology").value();
        req.set(ExecutionScope.class, new ExecutionScope(1000 * 60 * 10));

        ContentResponse<Object> response = controller.runCypher(query,ontology);

        RegistrarsUtils.with(req,resp, response);
    }
    //endregion
}
