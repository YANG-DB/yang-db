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

import com.cedarsoftware.util.io.JsonWriter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.graph.Graph;
import com.yangdb.fuse.dispatcher.urlSupplier.AppUrlSupplier;
import com.yangdb.fuse.logging.Route;
import com.yangdb.fuse.model.asgQuery.AsgQuery;
import com.yangdb.fuse.model.execution.plan.PlanWithCost;
import com.yangdb.fuse.model.execution.plan.composite.Plan;
import com.yangdb.fuse.model.execution.plan.costs.PlanDetailedCost;
import com.yangdb.fuse.model.execution.plan.descriptors.AsgQueryDescriptor;
import com.yangdb.fuse.model.execution.plan.descriptors.PlanWithCostDescriptor;
import com.yangdb.fuse.model.execution.plan.descriptors.QueryDescriptor;
import com.yangdb.fuse.model.query.Query;
import com.yangdb.fuse.model.resourceInfo.FuseError;
import com.yangdb.fuse.model.resourceInfo.QueryResourceInfo;
import com.yangdb.fuse.model.transport.*;
import com.yangdb.fuse.model.transport.cursor.LogicalGraphCursorRequest;
import com.yangdb.fuse.model.validation.ValidationResult;
import com.yangdb.fuse.rendering.SVGGraphRenderer;
import com.yangdb.fuse.services.controllers.QueryController;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.jooby.Response;
import org.jooby.*;
import org.jooby.apitool.ApiTool;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static org.jooby.Status.NOT_FOUND;
import static org.jooby.Status.OK;

public class QueryControllerRegistrar extends AppControllerRegistrarBase<QueryController> {
    /**
     * todo get this from application.conf
     */
    public static final int TIMEOUT = 1000 * 60 * 10;
    public static final int PAGE_SIZE = 1000;
    public static final String GRAPH_QL = "graphQL";

    //region Constructors
    public QueryControllerRegistrar() {
        super(QueryController.class);
    }
    //endregion

    //region AppControllerRegistrarBase Implementation
    @Override
    public void register(Jooby app, AppUrlSupplier appUrlSupplier) {
        /**
         * get the query store info
         * @return All queries in Query store information
         **/
        app.get(appUrlSupplier.queryStoreUrl(), req -> {
            Route.of("getQueryStore").write();
            return Results.with(this.getController(app).getInfo(), Status.OK);
        });

        /**
         * create a v1 query resource
         * @param  V1 Query Request
         * @return newly created query resource information
         **/
        app.post(appUrlSupplier.queryStoreUrl(),
                req -> API.postV1(app, req, this.getController(app)));

        /**  register graph API context **/
        graphApi(app, appUrlSupplier);

        /**  register V1 API context **/
        v1Context(app, appUrlSupplier);

        /**  register cypher API context **/
        cypherContext(app, appUrlSupplier);

        /**  register graphQL API context **/
        graphQLContext(app, appUrlSupplier);

        /**  register sparql API context **/
        sparqlContext(app, appUrlSupplier);

        /**  register sql API context **/
        sqlContext(app, appUrlSupplier);

        /** call a query */
        app.post(appUrlSupplier.queryStoreUrl() + "/call", req -> API.call(app, req, this.getController(app)));

        /** call a query */
        app.get(appUrlSupplier.resourceUrl(":queryId", ":cursorId") + "/nextPageData",
                req -> API.nextPage(app, req, this));

        /** get the query info */
        app.get(appUrlSupplier.resourceUrl(":queryId"), req -> {
            Route.of("getQuery").write();

            ContentResponse response = this.getController(app).getInfo(req.param("queryId").value());
            return Results.with(response, response.status());
        });

        /** delete a query */
        app.delete(appUrlSupplier.resourceUrl(":queryId"), req -> {
            Route.of("deleteQuery").write();
            ContentResponse response = this.getController(app).delete(req.param("queryId").value());
            return Results.with(response, response.status());
        });

        /** get the query verbose plan */
        app.get(appUrlSupplier.resourceUrl(":queryId") + "/planVerbose", req -> {
            ContentResponse response = this.getController(app).planVerbose(req.param("queryId").value());
            //temporary fix for json serialization of object graphs
            return Results.with(new ObjectMapper().writeValueAsString(response.getData()), response.status())
                    .type("application/json");
        });
        /** get the print of the execution plan */
        app.get(appUrlSupplier.resourceUrl(":queryId") + "/plan/print", req -> API.planPrint(app, req, this));

        /** get the query v1*/
        app.get(appUrlSupplier.resourceUrl(":queryId") + "/v1", req -> {
            ContentResponse response = this.getController(app).getV1(req.param("queryId").value());
            return Results.with(response, response.status());
        });

        /** get the query v1 print*/
        app.get(appUrlSupplier.resourceUrl(":queryId") + "/print",
                req -> API.printQuery(app, req, this));

        /** get the query v1 print*/
        app.get(appUrlSupplier.resourceUrl(":queryId") + "/visualize",
                (req,resp) -> API.queryView(app, req,resp, this));


        /** view the asg query with d3 html*/
        app.get(appUrlSupplier.resourceUrl(":queryId") + "/asg/visualize",
                (req,resp) -> API.asgView(app, req,resp, this));


        /** get the asg query */
        app.get(appUrlSupplier.resourceUrl(":queryId") + "/asg", req -> {
            ContentResponse<AsgQuery> response = this.getController(app).getAsg(req.param("queryId").value());
            return Results.with(response, response.status());
        });

        /** view the elastic query with d3 html*/
        app.get(appUrlSupplier.resourceUrl(":queryId") + "/elastic/view",
                req -> Results.redirect("/public/assets/ElasticQueryViewer.html?q=" +
                        appUrlSupplier.queryStoreUrl() + "/" + req.param("queryId").value() + "/elastic"));

        /** get the asg query */
        app.get(appUrlSupplier.resourceUrl(":queryId") + "/asg/json", req -> {
            ContentResponse<AsgQuery> response = this.getController(app).getAsg(req.param("queryId").value());
            return Results.json(response.getData());
        });

        /** get the asg query print*/
        app.get(appUrlSupplier.resourceUrl(":queryId") + "/asg/print",
                req -> API.printAsg(app, req, this));

        /** get the query plan execution */
        app.get(appUrlSupplier.resourceUrl(":queryId") + "/plan", req -> {
            ContentResponse response = this.getController(app).explain(req.param("queryId").value());
            //temporary fix for jason serialization of object graphs
            return Results.with(JsonWriter.objectToJson(response), response.status());
        });

        /** get the query verbose plan */
        app.get(appUrlSupplier.resourceUrl(":queryId") + "/plan/json", req -> {
            ContentResponse response = this.getController(app).explain(req.param("queryId").value());
            return Results.json(response.getData());
        });

        /** get the query verbose plan */
        app.get(appUrlSupplier.resourceUrl(":queryId") + "/plan/graph",
                req -> API.planGraph(app, req, this));

        /** get the query verbose plan */
        app.get(appUrlSupplier.resourceUrl(":queryId") + "/plan/visualize",
                (req,resp) -> API.planView(app, req,resp, this));

        //swagger
        app.use(new ApiTool()
                .swagger("/swagger")
                .raml("/raml")
        );

    }

    private void cypherContext(Jooby app, AppUrlSupplier appUrlSupplier) {
        /** create a cypher query */
        app.post(appUrlSupplier.queryStoreUrl() + "/cypher", req -> API.postCypher(app, req, this.getController(app)));
        /** run a cypher query (support both get/post protocols)  */
        app.get(appUrlSupplier.queryStoreUrl() + "/cypher/run", req -> API.runCypher(app, req, this.getController(app)));
        app.post(appUrlSupplier.queryStoreUrl() + "/cypher/run", req -> API.runCypher(app, req, this.getController(app)));
    }

    private void sparqlContext(Jooby app, AppUrlSupplier appUrlSupplier) {
        /** create a sparql query */
        app.post(appUrlSupplier.queryStoreUrl() + "/sparql", req -> API.postSparql(app, req, this.getController(app)));
        /** run a sparql query (support both get/post protocols) */
        app.get(appUrlSupplier.queryStoreUrl() + "/sparql/run", req -> API.runSparql(app, req, this.getController(app)));
        app.post(appUrlSupplier.queryStoreUrl() + "/sparql/run", req -> API.runSparql(app, req, this.getController(app)));
    }

    private void graphQLContext(Jooby app, AppUrlSupplier appUrlSupplier) {
        /** create a cypher query */
        app.post(appUrlSupplier.queryStoreUrl() + "/graphQL", req -> API.postGraphQL(app, req, this.getController(app)));
        /** run a cypher query (support both get/post protocols) */
        app.get(appUrlSupplier.queryStoreUrl() + "/graphQL/run", req -> API.runGraphQL(app, req, this.getController(app)));
        app.post(appUrlSupplier.queryStoreUrl() + "/graphQL/run", req -> API.runGraphQL(app, req, this.getController(app)));
    }

    private void sqlContext(Jooby app, AppUrlSupplier appUrlSupplier) {
        /** create a cypher query */
        app.post(appUrlSupplier.queryStoreUrl() + "/sql", req -> API.postSql(app, req, this.getController(app)));
        /** run a cypher query (support both get/post protocols) */
        app.get(appUrlSupplier.queryStoreUrl() + "/sql/run", req -> API.runSql(app, req, this.getController(app)));
        app.post(appUrlSupplier.queryStoreUrl() + "/sql/run", req -> API.runSql(app, req, this.getController(app)));
    }

    private void v1Context(Jooby app, AppUrlSupplier appUrlSupplier) {
        /** validate a v1 query */
        app.post(appUrlSupplier.queryStoreUrl() + "/v1/validate", req -> API.validateV1(app, req, this.getController(app)));

        /** get the plan from v1 query */
        app.post(appUrlSupplier.queryStoreUrl() + "/v1/plan", req -> API.plan(app, req, this.getController(app)));

        /** get the traversal from v1 query */
        app.post(appUrlSupplier.queryStoreUrl() + "/v1/traversal", req -> API.traversal(app, req, this.getController(app)));

        /** create a v1 query */
        app.post(appUrlSupplier.queryStoreUrl() + "/v1", req -> API.postV1(app, req, this.getController(app)));

        /** create a v1 query */
        app.post(appUrlSupplier.queryStoreUrl() + "/v1/run", req -> API.runV1(app, req, this.getController(app)));
    }

    private void graphApi(Jooby app, AppUrlSupplier appUrlSupplier) {
        app.get(appUrlSupplier.queryStoreUrl() + "/graph/api/findPath", req -> API.findPath(app, req, this.getController(app)));
        app.get(appUrlSupplier.queryStoreUrl() + "/graph/api/getVertex", req -> API.getVertex(app, req, this.getController(app)));
        app.get(appUrlSupplier.queryStoreUrl() + "/graph/api/getNeighbors", req -> API.getNeighbors(app, req, this.getController(app)));
    }
    //endregion


    public static class API {

        public static final String CYPHER = "cypher";
        public static final String SPARQL = "sparql";
        public static final String SQL = "sql";

        public static Result postGraphQL(Jooby app, final Request req, QueryController controller) throws Exception {
            Route.of("postGraphQL").write();

            CreateJsonQueryRequest createQueryRequest = req.body(CreateJsonQueryRequest.class);
            req.set(CreateJsonQueryRequest.class, createQueryRequest);
            req.set(PlanTraceOptions.class, createQueryRequest.getPlanTraceOptions());
            final long maxExecTime = createQueryRequest.getCreateCursorRequest() != null
                    ? createQueryRequest.getCreateCursorRequest().getMaxExecutionTime() : 0;
            req.set(ExecutionScope.class, new ExecutionScope(Math.max(maxExecTime, TIMEOUT)));

            ContentResponse<QueryResourceInfo> response = createQueryRequest.getCreateCursorRequest() == null ?
                    controller.create(createQueryRequest) :
                    controller.createAndFetch(createQueryRequest);

            return Results.with(response, response.status());

        }

        public static Result postSql(Jooby app, final Request req, QueryController controller) throws Exception {
            Route.of("postSql").write();

            CreateJsonQueryRequest createQueryRequest = req.body(CreateJsonQueryRequest.class);
            req.set(CreateJsonQueryRequest.class, createQueryRequest);
            req.set(PlanTraceOptions.class, createQueryRequest.getPlanTraceOptions());
            final long maxExecTime = createQueryRequest.getCreateCursorRequest() != null
                    ? createQueryRequest.getCreateCursorRequest().getMaxExecutionTime() : 0;
            req.set(ExecutionScope.class, new ExecutionScope(Math.max(maxExecTime, TIMEOUT)));

            ContentResponse<QueryResourceInfo> response = createQueryRequest.getCreateCursorRequest() == null ?
                    controller.create(createQueryRequest) :
                    controller.createAndFetch(createQueryRequest);

            return Results.with(response, response.status());

        }

        public static Result validateV1(Jooby app, final Request req, QueryController controller) throws Exception {
            Route.of("validateAndRewriteQuery").write();

            Query query = req.body(Query.class);
            req.set(Query.class, query);
            ContentResponse<ValidationResult> response = controller.validate(query);

            return Results.json(response.getData());

        }

        public static Result findPath(Jooby app, final Request req, QueryController controller) {
            Route.of("findPath").write();

            req.set(ExecutionScope.class, new ExecutionScope(TIMEOUT));

            ContentResponse<Object> response = controller.findPath(
                    req.param("ontology").value(),
                    req.param("sourceEntity").value(),
                    req.param("sourceId").value(),
                    req.param("targetEntity").value(),
                    req.param("targetId").value(),
                    req.param("relationType").value(),
                    req.param("maxHops").intValue());

            return Results.json(response.getData());

        }

        public static Result getVertex(Jooby app, final Request req, QueryController controller) {
            Route.of("getVertex").write();

            req.set(ExecutionScope.class, new ExecutionScope(TIMEOUT));

            ContentResponse<Object> response = controller.getVertex(
                    req.param("ontology").value(),
                    req.param("type").value(),
                    req.param("id").value());

            return Results.json(response.getData());

        }

        public static Result getNeighbors(Jooby app, final Request req, QueryController controller) {
            Route.of("getNeighbors").write();

            req.set(ExecutionScope.class, new ExecutionScope(TIMEOUT));

            ContentResponse<Object> response = controller.getNeighbors(
                    req.param("ontology").value(),
                    req.param("type").value(),
                    req.param("id").value());

            return Results.json(response.getData());

        }

        public static Result plan(Jooby app, final Request req, QueryController controller) throws Exception {
            Route.of("planByQuery").write();

            Query query = req.body(Query.class);
            req.set(Query.class, query);
            ContentResponse<PlanWithCost<Plan, PlanDetailedCost>> response = controller.plan(query);

            return Results.json(response.getData().toString());

        }

        public static Result traversal(Jooby app, final Request req, QueryController controller) throws Exception {
            Route.of("traversal").write();

            Query query = req.body(Query.class);
            req.set(Query.class, query);
            ContentResponse<GraphTraversal> response = controller.traversal(query);

            return Results.with(response.getData().explain().prettyPrint());

        }

        public static Result postV1(Jooby app, final Request req, QueryController controller) throws Exception {
            Route.of("postQuery").write();

            CreateQueryRequest createQueryRequest = req.body(CreateQueryRequest.class);
            req.set(CreateQueryRequest.class, createQueryRequest);
            req.set(PlanTraceOptions.class, createQueryRequest.getPlanTraceOptions());
            final long maxExecTime = createQueryRequest.getCreateCursorRequest() != null
                    ? createQueryRequest.getCreateCursorRequest().getMaxExecutionTime() : 0;
            req.set(ExecutionScope.class, new ExecutionScope(Math.max(maxExecTime, TIMEOUT)));

            ContentResponse<QueryResourceInfo> response = createQueryRequest.getCreateCursorRequest() == null ?
                    controller.create(createQueryRequest) :
                    controller.createAndFetch(createQueryRequest);

            return Results.with(response, response.status());
        }

        public static Result postCypher(Jooby app, final Request req, QueryController controller) throws Exception {
            Route.of("postCypher").write();

            CreateJsonQueryRequest createQueryRequest = req.body(CreateJsonQueryRequest.class);
            req.set(CreateJsonQueryRequest.class, createQueryRequest);
            req.set(PlanTraceOptions.class, createQueryRequest.getPlanTraceOptions());
            final long maxExecTime = createQueryRequest.getCreateCursorRequest() != null
                    ? createQueryRequest.getCreateCursorRequest().getMaxExecutionTime() : 0;
            req.set(ExecutionScope.class, new ExecutionScope(Math.max(maxExecTime, TIMEOUT)));

            ContentResponse<QueryResourceInfo> response = createQueryRequest.getCreateCursorRequest() == null ?
                    controller.create(createQueryRequest) :
                    controller.createAndFetch(createQueryRequest);

            return Results.with(response, response.status());

        }

        public static Result postSparql(Jooby app, final Request req, QueryController controller) throws Exception {
            Route.of("postSparql").write();

            CreateJsonQueryRequest createQueryRequest = req.body(CreateJsonQueryRequest.class);
            req.set(CreateJsonQueryRequest.class, createQueryRequest);
            req.set(PlanTraceOptions.class, createQueryRequest.getPlanTraceOptions());
            final long maxExecTime = createQueryRequest.getCreateCursorRequest() != null
                    ? createQueryRequest.getCreateCursorRequest().getMaxExecutionTime() : 0;
            req.set(ExecutionScope.class, new ExecutionScope(Math.max(maxExecTime, TIMEOUT)));

            ContentResponse<QueryResourceInfo> response = createQueryRequest.getCreateCursorRequest() == null ?
                    controller.create(createQueryRequest) :
                    controller.createAndFetch(createQueryRequest);

            return Results.with(response, response.status());

        }

        public static Result runV1(Jooby app, final Request req, QueryController controller) throws Exception {
            Route.of("runQuery").write();

            Query query = req.body(Query.class);
            req.set(Query.class, query);
            req.set(ExecutionScope.class, new ExecutionScope(TIMEOUT));

            ContentResponse<Object> response = controller.runV1Query(query,
                    req.param("pageSize").isSet() ? req.param("pageSize").intValue() : PAGE_SIZE,
                    req.param("cursorType").isSet() ? req.param("cursorType").value() : LogicalGraphCursorRequest.CursorType
            );

            return Results.with(response, response.status());
        }

        public static Result runCypher(Jooby app, final Request req, QueryController controller) throws Throwable {
            Route.of("runCypher").write();

            Optional<String> query;
            if (req.param(CYPHER).isSet()) {
                query = Optional.of(req.param(CYPHER).value());
            } else {
                query = Optional.of(req.body(String.class));
            }

            //verify query value exists
            query.orElseThrow(
                    () -> new FuseError.FuseErrorException(new FuseError("Request Error", "No query parameter found in request")));

            String ontology = req.param("ontology").value();
            req.set(ExecutionScope.class, new ExecutionScope(TIMEOUT));

            ContentResponse<Object> response = controller.runCypher(query.get(), ontology,
                    req.param("pageSize").isSet() ? req.param("pageSize").intValue() : PAGE_SIZE,
                    req.param("cursorType").isSet() ? req.param("cursorType").value() : LogicalGraphCursorRequest.CursorType
            );

            return Results.with(response, response.status());
        }

        public static Result runSparql(Jooby app, final Request req, QueryController controller) throws Throwable {
            Route.of("runSparql").write();

            Optional<String> query;
            if (req.param(SPARQL).isSet()) {
                query = Optional.of(req.param(SPARQL).value());
            } else {
                query = Optional.of(req.body(String.class));
            }

            //verify query value exists
            query.orElseThrow(
                    () -> new FuseError.FuseErrorException(new FuseError("Request Error", "No query parameter found in request")));

            String ontology = req.param("ontology").value();
            req.set(ExecutionScope.class, new ExecutionScope(TIMEOUT));

            ContentResponse<Object> response = controller.runSparql(query.get(), ontology,
                    req.param("pageSize").isSet() ? req.param("pageSize").intValue() : PAGE_SIZE,
                    req.param("cursorType").isSet() ? req.param("cursorType").value() : LogicalGraphCursorRequest.CursorType
            );

            return Results.with(response, response.status());
        }

        public static Result runGraphQL(Jooby app, final Request req, QueryController controller) throws Throwable {
            Route.of("runGraphQL").write();

            Optional<String> query;
            if (req.param(GRAPH_QL).isSet()) {
                query = Optional.of(req.param(GRAPH_QL).value());
            } else {
                query = Optional.of(req.body(String.class));
            }

            //verify query value exists
            query.orElseThrow(
                    () -> new FuseError.FuseErrorException(new FuseError("Request Error", "No query parameter found in request")));

            String ontology = req.param("ontology").value();
            req.set(ExecutionScope.class, new ExecutionScope(TIMEOUT));

            ContentResponse<Object> response = controller.runGraphQL(query.get(), ontology,
                    req.param("pageSize").isSet() ? req.param("pageSize").intValue() : PAGE_SIZE,
                    req.param("cursorType").isSet() ? req.param("cursorType").value() : LogicalGraphCursorRequest.CursorType
            );

            return Results.with(response, response.status());
        }

        public static Result runSql(Jooby app, final Request req, QueryController controller) throws Throwable {
            Route.of("runSql").write();

            Optional<String> query;
            if (req.param(SQL).isSet()) {
                query = Optional.of(req.param(SQL).value());
            } else {
                query = Optional.of(req.body(String.class));
            }

            //verify query value exists
            query.orElseThrow(
                    () -> new FuseError.FuseErrorException(new FuseError("Request Error", "No query parameter found in request")));

            String ontology = req.param("ontology").value();
            req.set(ExecutionScope.class, new ExecutionScope(TIMEOUT));

            ContentResponse<Object> response = controller.runSqlQuery(query.get(), ontology,
                    req.param("pageSize").isSet() ? req.param("pageSize").intValue() : PAGE_SIZE,
                    req.param("cursorType").isSet() ? req.param("cursorType").value() : LogicalGraphCursorRequest.CursorType
            );

            return Results.with(response, response.status());
        }

        public static Result call(Jooby app, Request req, QueryController controller) throws Exception {
            Route.of("callQuery").write();

            ExecuteStoredQueryRequest callQueryRequest = req.body(ExecuteStoredQueryRequest.class);
            req.set(ExecuteStoredQueryRequest.class, callQueryRequest);
            req.set(PlanTraceOptions.class, callQueryRequest.getPlanTraceOptions());
            final long maxExecTime = callQueryRequest.getCreateCursorRequest() != null
                    ? callQueryRequest.getCreateCursorRequest().getMaxExecutionTime() : 0;
            req.set(ExecutionScope.class, new ExecutionScope(Math.max(maxExecTime, TIMEOUT)));
            ContentResponse<QueryResourceInfo> response = controller.callAndFetch(callQueryRequest);

//            return with(req,response);
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

        public static Result printAsg(Jooby app, Request req, QueryControllerRegistrar registrar) {
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
            String print = PlanWithCostDescriptor.print(response.getData(), true);
            ContentResponse<String> compose = ContentResponse.Builder.<String>builder(OK, NOT_FOUND)
                    .data(Optional.of(print))
                    .compose();
            return Results.with(compose, compose.status());
        }

        public static Result printQuery(Jooby app, Request req, QueryControllerRegistrar registrar) {
            ContentResponse<Query> response = registrar.getController(app).getV1(req.param("queryId").value());
            String print = QueryDescriptor.print(response.getData());
            ContentResponse<String> compose = ContentResponse.Builder.<String>builder(OK, NOT_FOUND)
                    .data(Optional.of(print))
                    .compose();
            return Results.with(compose, compose.status());

        }

        /**
         * print an SVG representation of the V1 query
         * @param app
         * @param req
         * @param resp
         * @param registrar
         * @return
         * @throws Throwable
         */
        public static Object queryView(Jooby app, Request req, Response resp, QueryControllerRegistrar registrar) throws Throwable {
            String queryId = req.param("queryId").value();
            ContentResponse<Query> response = registrar.getController(app).getV1(queryId);
            String dotGraph = QueryDescriptor.printGraph(response.getData());
            File file = SVGGraphRenderer.render(queryId, dotGraph);
            ContentResponse<File> compose = ContentResponse.Builder.<File>builder(OK, NOT_FOUND)
                    .data(file)
                    .compose();
            return RegistrarsUtils.withImg(req,resp,compose);
        }

        /**
          * print an SVG representation of the ASG query
         * @param app
         * @param req
         * @param resp
         * @param registrar
         * @return
         * @throws Throwable
         */
        public static Object asgView(Jooby app, Request req, Response resp, QueryControllerRegistrar registrar) throws Throwable {
            String queryId = req.param("queryId").value();
            ContentResponse<AsgQuery> response = registrar.getController(app).getAsg(queryId);
            String dotGraph = AsgQueryDescriptor.printGraph(response.getData());
            File file = SVGGraphRenderer.render(queryId, dotGraph);
            ContentResponse<File> compose = ContentResponse.Builder.<File>builder(OK, NOT_FOUND)
                    .data(file)
                    .compose();
            return RegistrarsUtils.withImg(req,resp,compose);
        }

        /**
         * print an SVG representation of the execution plan
         * @param app
         * @param req
         * @param resp
         * @param registrar
         * @return
         * @throws Throwable
         */
        public static Object planView(Jooby app, Request req, Response resp, QueryControllerRegistrar registrar) throws Throwable {
            String queryId = req.param("queryId").value();
            ContentResponse<PlanWithCost<Plan, PlanDetailedCost>> response = registrar.getController(app).explain(req.param("queryId").value());
            String dotGraph = PlanWithCostDescriptor.printGraph(response.getData());
            File file = SVGGraphRenderer.render(queryId, dotGraph);
            ContentResponse<File> compose = ContentResponse.Builder.<File>builder(OK, NOT_FOUND)
                    .data(file)
                    .compose();
            return RegistrarsUtils.withImg(req,resp,compose);
        }

    }
}
