package com.yangdb.fuse.services.appRegistrars;

/*-
 * #%L
 * fuse-service
 * %%
 * Copyright (C) 2016 - 2018 yangdb   ------ www.yangdb.org ------
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
import com.yangdb.fuse.model.ontology.Ontology;
import com.yangdb.fuse.model.transport.ContentResponse;
import com.yangdb.fuse.services.controllers.CatalogController;
import org.jooby.Jooby;
import org.jooby.Results;

import java.util.List;

public class CatalogControllerRegistrar extends AppControllerRegistrarBase<CatalogController> {
    //region Constructors
    public CatalogControllerRegistrar() {
        super(CatalogController.class);
    }
    //endregion

    //region AppControllerRegistrarBase Implementation
    @Override
    public void register(Jooby app, AppUrlSupplier appUrlSupplier) {
        /** get available ontologies*/
        app.get("/fuse/catalog/ontology"
                ,req -> {
                    ContentResponse<List<Ontology>> response = this.getController(app).getOntologies();
                    return Results.with(response, response.status());
                });

        /** get the ontology by id */
        app.get("/fuse/catalog/ontology/:id"
                ,req -> {
                    ContentResponse response = this.getController(app).getOntology(req.param("id").value());
                    return Results.with(response, response.status());
                });

        /** get available schemas **/
        app.get("/fuse/catalog/schema"
                ,req -> {
                    ContentResponse<List<String>> response = this.getController(app).getSchemas();
                    return Results.with(response, response.status());
                });

        app.get("/fuse/catalog/schema/:id",
                req -> {
                    ContentResponse response = this.getController(app).getSchema(req.param("id").value());
                    return Results.with(response, response.status());
                });
    }
    //endregion
}
