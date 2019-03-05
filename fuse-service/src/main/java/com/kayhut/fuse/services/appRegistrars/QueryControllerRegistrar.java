package com.kayhut.fuse.services.appRegistrars;

/*-
 * #%L
 * fuse-service
 * %%
 * Copyright (C) 2016 - 2018 kayhut
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

import com.cedarsoftware.util.io.JsonWriter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.graph.Graph;
import com.kayhut.fuse.dispatcher.urlSupplier.AppUrlSupplier;
import com.kayhut.fuse.logging.Route;
import com.kayhut.fuse.model.asgQuery.AsgQuery;
import com.kayhut.fuse.model.execution.plan.PlanWithCost;
import com.kayhut.fuse.model.execution.plan.composite.Plan;
import com.kayhut.fuse.model.execution.plan.costs.PlanDetailedCost;
import com.kayhut.fuse.model.execution.plan.descriptors.AsgQueryDescriptor;
import com.kayhut.fuse.model.execution.plan.descriptors.PlanWithCostDescriptor;
import com.kayhut.fuse.model.execution.plan.descriptors.QueryDescriptor;
import com.kayhut.fuse.model.query.Query;
import com.kayhut.fuse.model.resourceInfo.QueryResourceInfo;
import com.kayhut.fuse.model.transport.*;
import com.kayhut.fuse.model.transport.Response;
import com.kayhut.fuse.services.controllers.QueryController;
import org.jooby.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static com.kayhut.fuse.services.appRegistrars.QueryControllerRegistrar.API.*;
import static org.jooby.Status.NOT_FOUND;
import static org.jooby.Status.OK;

public class QueryControllerRegistrar extends AppControllerRegistrarBase<QueryController> {
    //region Constructors
    public QueryControllerRegistrar() {
        super(QueryController.class);
    }
    //endregion

    //region AppControllerRegistrarBase Implementation
    @Override
    public void register(Jooby app, AppUrlSupplier appUrlSupplier) {
        /** get the query store info */
        app.get(appUrlSupplier.queryStoreUrl(),req -> {
                    Route.of("getQueryStore").write();
                    return Results.with(this.getController(app).getInfo(), Status.OK);
                });

        /** create a v1 fallback url */
        app.post(appUrlSupplier.queryStoreUrl() ,
                req -> postV1(app,req,this));

        /** create a v1 query */
        app.post(appUrlSupplier.queryStoreUrl() + "/v1",req -> postV1(app,req,this));

        /** create a v1 query */
        app.post(appUrlSupplier.queryStoreUrl() + "/v1/run",req -> runV1(app,req,this));

        /** create a cypher query */
        app.post(appUrlSupplier.queryStoreUrl() + "/cypher",req -> postCypher(app,req,this));


        /** call a query */
        app.post(appUrlSupplier.queryStoreUrl() + "/call",req -> API.call(app,req,this));

        /** call a query */
        app.get(appUrlSupplier.resourceUrl(":queryId") + "/nextPageData",
                req -> API.nextPage(app,req,this));

        /** get the query info */
        app.get(appUrlSupplier.resourceUrl(":queryId"),req -> {
                    Route.of("getQuery").write();

                    ContentResponse response = this.getController(app).getInfo(req.param("queryId").value());
                    return Results.with(response, response.status());
                });

        /** delete a query */
        app.delete(appUrlSupplier.resourceUrl(":queryId"),req -> {
                    Route.of("deleteQuery").write();
                    ContentResponse response = this.getController(app).delete(req.param("queryId").value());
                    return Results.with(response, response.status());
                });

        /** get the query verbose plan */
        app.get(appUrlSupplier.resourceUrl(":queryId") + "/planVerbose",req -> {
                    ContentResponse response = this.getController(app).planVerbose(req.param("queryId").value());
                    //temporary fix for json serialization of object graphs
                    return Results.with(new ObjectMapper().writeValueAsString(response.getData()), response.status())
                            .type("application/json");
                });
        /** get the print of the execution plan */
        app.get(appUrlSupplier.resourceUrl(":queryId") + "/plan/print",req -> API.planPrint(app,req,this));

        /** get the query v1*/
        app.get(appUrlSupplier.resourceUrl(":queryId") + "/v1",req -> {
                    ContentResponse response = this.getController(app).getV1(req.param("queryId").value());
                    return Results.with(response, response.status());
                });
        /** get the query v1 print*/
        app.get(appUrlSupplier.resourceUrl(":queryId") + "/v1/print",req -> API.queryPrint(app,req,this));


        /** view the asg query with d3 html*/
        app.get(appUrlSupplier.resourceUrl(":queryId") + "/asg/view",
                req -> Results.redirect("/public/assets/AsgTreeViewer.html?q=" + req.param("queryId").value()));


        /** get the asg query */
        app.get(appUrlSupplier.resourceUrl(":queryId") + "/asg",req -> {
                    ContentResponse<AsgQuery> response = this.getController(app).getAsg(req.param("queryId").value());
                    return Results.with(response, response.status());
                });

        /** view the elastic query with d3 html*/
        app.get(appUrlSupplier.resourceUrl(":queryId") + "/elastic/view",
                req -> Results.redirect("/public/assets/ElasticQueryViewer.html?q=" +
                        appUrlSupplier.queryStoreUrl() + "/" + req.param("queryId").value() + "/elastic"));

        /** get the asg query */
        app.get(appUrlSupplier.resourceUrl(":queryId") + "/asg/json",req -> {
                    ContentResponse<AsgQuery> response = this.getController(app).getAsg(req.param("queryId").value());
                    return Results.json(response.getData());
                });

        /** get the asg query print*/
        app.get(appUrlSupplier.resourceUrl(":queryId") + "/asg/print",
                req -> API.print(app,req,this));

        /** get the query plan execution */
        app.get(appUrlSupplier.resourceUrl(":queryId") + "/plan",req -> {
                    ContentResponse response = this.getController(app).explain(req.param("queryId").value());
                    //temporary fix for jason serialization of object graphs
                    return Results.with(JsonWriter.objectToJson(response), response.status());
                });

        /** get the query verbose plan */
        app.get(appUrlSupplier.resourceUrl(":queryId") + "/plan/json",req -> {
                    ContentResponse response = this.getController(app).explain(req.param("queryId").value());
                    return Results.json(response.getData());
                });

        /** get the query verbose plan */
        app.get(appUrlSupplier.resourceUrl(":queryId") + "/plan/graph",
                req -> API.planGraph(app,req,this));

        app.get(appUrlSupplier.resourceUrl(":queryId") + "/plan/view",
                req -> Results.redirect("/public/assets/PlanTreeViewer.html?q=" + req.param("queryId").value()));

        app.get(appUrlSupplier.resourceUrl(":queryId") + "/plan/sankey",
                req -> Results.redirect("/public/assets/PlanSankeyViewer.html?q=" + req.param("queryId").value()));
    }
    //endregion


    static class API {
        public static Result postCypher(Jooby app, final Request req, QueryControllerRegistrar registrar  ) throws Exception {
            Route.of("postCypher").write();

            CreateJsonQueryRequest createQueryRequest = req.body(CreateJsonQueryRequest.class);
            req.set(CreateJsonQueryRequest.class, createQueryRequest);
            req.set(PlanTraceOptions.class, createQueryRequest.getPlanTraceOptions());
            final long maxExecTime = createQueryRequest.getCreateCursorRequest() != null
                    ? createQueryRequest.getCreateCursorRequest().getMaxExecutionTime() : 0;
            req.set(ExecutionScope.class, new ExecutionScope(Math.max(maxExecTime, 1000 * 60 * 10)));

            ContentResponse<QueryResourceInfo> response = createQueryRequest.getCreateCursorRequest() == null ?
                    registrar.getController(app).create(createQueryRequest) :
                    registrar.getController(app).createAndFetch(createQueryRequest);

            return Results.with(response, response.status());

        }

        public static Result postV1(Jooby app, final Request req, QueryControllerRegistrar registrar  ) throws Exception {
            Route.of("postQuery").write();

            CreateQueryRequest createQueryRequest = req.body(CreateQueryRequest.class);
            req.set(CreateQueryRequest.class, createQueryRequest);
            req.set(PlanTraceOptions.class, createQueryRequest.getPlanTraceOptions());
            final long maxExecTime = createQueryRequest.getCreateCursorRequest() != null
                    ? createQueryRequest.getCreateCursorRequest().getMaxExecutionTime() : 0;
            req.set(ExecutionScope.class, new ExecutionScope(Math.max(maxExecTime, 1000 * 60 * 10)));

            ContentResponse<QueryResourceInfo> response = createQueryRequest.getCreateCursorRequest() == null ?
                    registrar.getController(app).create(createQueryRequest) :
                    registrar.getController(app).createAndFetch(createQueryRequest);

            return Results.with(response, response.status());
        }

        public static Result runV1(Jooby app, final Request req, QueryControllerRegistrar registrar  ) throws Exception {
            Route.of("runQuery").write();

            Query query = req.body(Query.class);
            req.set(Query.class, query);
            req.set(ExecutionScope.class, new ExecutionScope(1000 * 60 * 10));

            ContentResponse<Object> response = registrar.getController(app).run(query);

            return Results.with(response, response.status());
        }

        public static Result call(Jooby app, Request req, QueryControllerRegistrar registrar) throws Exception {
            Route.of("callQuery").write();

            ExecuteStoredQueryRequest callQueryRequest = req.body(ExecuteStoredQueryRequest.class);
            req.set(ExecuteStoredQueryRequest.class, callQueryRequest);
            req.set(PlanTraceOptions.class, callQueryRequest.getPlanTraceOptions());
            final long maxExecTime = callQueryRequest.getCreateCursorRequest() != null
                    ? callQueryRequest.getCreateCursorRequest().getMaxExecutionTime() : 0;
            req.set(ExecutionScope.class, new ExecutionScope(Math.max(maxExecTime, 1000 * 60 * 10)));
            ContentResponse<QueryResourceInfo> response = registrar.getController(app).callAndFetch(callQueryRequest);

            return Results.with(response, response.status());

        }

        public static Result nextPage(Jooby app, Request req, QueryControllerRegistrar registrar) {
            Route.of("nextPageData").write();
            ContentResponse<Object> page = registrar.getController(app)
                    .fetchNextPage(req.param("queryId").value(),
                            req.param("cursorId").toOptional(String.class),
                            req.param("pageSize").intValue(),
                            req.param("deletePage").isSet() ? req.param("deletePage").booleanValue() : true);
            return Results.with(page, page.status());

        }

        public static Result print(Jooby app, Request req, QueryControllerRegistrar registrar) {
            ContentResponse<AsgQuery> response = registrar.getController(app).getAsg(req.param("queryId").value());
            String print = AsgQueryDescriptor.print(response.getData());
            ContentResponse<String> compose = ContentResponse.Builder.<String>builder(OK, NOT_FOUND)
                    .data(Optional.of(print))
                    .compose();
            return Results.with(compose, compose.status());

        }

        public static Result planGraph(Jooby app, Request req, QueryControllerRegistrar registrar) {
            ContentResponse response = registrar.getController(app).explain(req.param("queryId").value());
            Boolean cycle = Boolean.valueOf(req.param("cycle").toOptional().orElse("true"));
            Graph<PlanWithCostDescriptor.GraphElement> graph = PlanWithCostDescriptor.graph((PlanWithCost<Plan, PlanDetailedCost>) response.getData(), cycle);
            Map<String, Set> map = new HashMap<>();
            map.put("nodes", graph.nodes());
            map.put("edges", graph.edges().stream().map(v -> new Object[]{v.nodeU(), v.nodeV()}).collect(Collectors.toSet()));
            return Results.json(map);
        }

        public static Result planPrint(Jooby app, Request req, QueryControllerRegistrar registrar) {
            ContentResponse<PlanWithCost<Plan, PlanDetailedCost>> response = registrar.getController(app).explain(req.param("queryId").value());
            String print = PlanWithCostDescriptor.print(response.getData());
            ContentResponse<String> compose = ContentResponse.Builder.<String>builder(OK, NOT_FOUND)
                    .data(Optional.of(print))
                    .compose();
            return Results.with(compose, compose.status());
        }

        public static Result queryPrint(Jooby app, Request req, QueryControllerRegistrar registrar) {
            ContentResponse<Query> response = registrar.getController(app).getV1(req.param("queryId").value());
            String print = QueryDescriptor.print(response.getData());
            ContentResponse<String> compose = ContentResponse.Builder.<String>builder(OK, NOT_FOUND)
                    .data(Optional.of(print))
                    .compose();
            return Results.with(compose, compose.status());

        }
    }
}
