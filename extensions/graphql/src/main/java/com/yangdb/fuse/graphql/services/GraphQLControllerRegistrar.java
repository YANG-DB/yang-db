package com.yangdb.fuse.graphql.services;

/*-
 * #%L
 * fuse-extension-graphql
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
import com.yangdb.fuse.graphql.services.controllers.StandardGraphQLController;
import com.yangdb.fuse.logging.Route;
import com.yangdb.fuse.model.ontology.Ontology;
import com.yangdb.fuse.model.transport.ContentResponse;
import com.yangdb.fuse.model.transport.CreateQueryRequestMetadata;
import com.yangdb.fuse.services.appRegistrars.AppControllerRegistrarBase;
import com.yangdb.fuse.services.appRegistrars.QueryControllerRegistrar;
import com.yangdb.fuse.services.controllers.QueryController;
import org.jooby.Jooby;
import org.jooby.Results;

/**
 * The GraphQL routes:
 * <ul>
 *     <li>{@code POST /fuse/graphql/ontology/{id}}: translate a GraphQL SDL schema to an ontology</li>
 *     <li>{@code POST /fuse/query/graphQL}: create a graphQL query</li>
 *     <li>{@code GET|POST /fuse/query/graphQL/run}: run one; the query is the {@code graphQL} or {@code query} parameter,
 *     or the body. The same as the generic {@code /fuse/query/graphQL/run} route, kept for its {@code graphQL}
 *     parameter.</li>
 * </ul>
 */
public class GraphQLControllerRegistrar extends AppControllerRegistrarBase<StandardGraphQLController> {
    /** the query text parameter of the graphQL run route */
    public static final String GRAPH_QL = "graphQL";

    //region Constructors
    public GraphQLControllerRegistrar() {
        super(StandardGraphQLController.class);
    }
    //endregion

    //region AppControllerRegistrarBase Implementation
    @Override
    public void register(Jooby app, AppUrlSupplier appUrlSupplier) {
        /** translate a GraphQL schema to an ontology */
        app.post("/fuse/graphql/ontology/:id"
                ,req -> {
                    Route.of("translateGraphQLSchema").write();
                    String graphQLSchemas = req.body(String.class);
                    req.set(String.class, graphQLSchemas);
                    ContentResponse<Ontology> response = this.getController(app).translate(req.param("id").value(), graphQLSchemas);
                    return Results.with(response, response.status().getStatus());
                });

        /** create a graphQL query */
        app.post(appUrlSupplier.queryStoreUrl() + "/" + GRAPH_QL, req -> QueryControllerRegistrar.API.postJsonQuery(
                "postGraphQL", req, CreateQueryRequestMetadata.TYPE_GRAPHQL, app.require(QueryController.class)));
        /** run a graphQL query (get / post) */
        app.get(appUrlSupplier.queryStoreUrl() + "/" + GRAPH_QL + "/run", (req, res) -> runGraphQL(app, req, res));
        app.post(appUrlSupplier.queryStoreUrl() + "/" + GRAPH_QL + "/run", (req, res) -> runGraphQL(app, req, res));
    }
    //endregion

    private static void runGraphQL(Jooby app, org.jooby.Request req, org.jooby.Response res) throws Throwable {
        QueryController controller = app.require(QueryController.class);
        QueryControllerRegistrar.API.runJsonQuery("runGraphQL", req, res, GRAPH_QL, (query, ontology, pageSize, cursorType) ->
                controller.run(query, CreateQueryRequestMetadata.TYPE_GRAPHQL, ontology, pageSize, cursorType));
    }
}
