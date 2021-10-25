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
import com.yangdb.fuse.services.controllers.languages.sparql.StandardSparqlController;
import org.jooby.Jooby;
import org.jooby.Results;

public class SparqlControllerRegistrar extends AppControllerRegistrarBase<StandardSparqlController> {
    //region Constructors
    public SparqlControllerRegistrar() {
        super(StandardSparqlController.class);
    }
    //endregion

    //region AppControllerRegistrarBase Implementation
    @Override
    public void register(Jooby app, AppUrlSupplier appUrlSupplier) {
        /** create new ontology*/
        app.post("/fuse/sparql/ontology/:id"
                ,req -> {
                    Route.of("translateOwlSchema").write();
                    String owlSchmea = req.body(String.class);
                    req.set(String.class, owlSchmea);
                    ContentResponse<Ontology> response = this.getController(app).translate(req.param("id").value(), owlSchmea);
                    return Results.with(response, response.status().getStatus());
                });
    }
    //endregion
}
