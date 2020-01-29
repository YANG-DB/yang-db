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

import com.yangdb.fuse.dispatcher.urlSupplier.AppUrlSupplier;
import com.yangdb.fuse.logging.Route;
import com.yangdb.fuse.model.ontology.Ontology;
import com.yangdb.fuse.model.transport.ContentResponse;
import com.yangdb.fuse.services.controllers.CatalogController;
import com.yangdb.fuse.services.controllers.GraphQLController;
import org.jooby.Jooby;
import org.jooby.Results;

import java.util.List;

public class GraphQLControllerRegistrar extends AppControllerRegistrarBase<GraphQLController> {
    //region Constructors
    public GraphQLControllerRegistrar() {
        super(GraphQLController.class);
    }
    //endregion

    //region AppControllerRegistrarBase Implementation
    @Override
    public void register(Jooby app, AppUrlSupplier appUrlSupplier) {
        /** create new ontology*/
        app.post("/fuse/graphql/ontology"
                ,req -> {
                    Route.of("translateGraphQLSchema").write();
                    String graphQLSchemas = req.body(String.class);
                    req.set(String.class, graphQLSchemas);
                    ContentResponse<Ontology> response = this.getController(app).translate(graphQLSchemas);
                    return Results.with(response, response.status());
                });
    }
    //endregion
}
