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
import com.yangdb.fuse.model.execution.plan.descriptors.OntologyDescriptor;
import com.yangdb.fuse.model.ontology.Ontology;
import com.yangdb.fuse.model.transport.ContentResponse;
import com.yangdb.fuse.model.transport.Status;
import com.yangdb.fuse.rendering.SVGGraphRenderer;
import com.yangdb.fuse.services.controllers.CatalogController;
import org.jooby.Jooby;
import org.jooby.Request;
import org.jooby.Response;
import org.jooby.Results;

import java.io.File;
import java.util.List;

import static org.jooby.Status.NOT_FOUND;
import static org.jooby.Status.OK;

public class CatalogControllerRegistrar extends AppControllerRegistrarBase<CatalogController> {
    //region Constructors
    public CatalogControllerRegistrar() {
        super(CatalogController.class);
    }
    //endregion

    //region AppControllerRegistrarBase Implementation
    @Override
    public void register(Jooby app, AppUrlSupplier appUrlSupplier) {
        /** create new ontology*/
        app.post("/fuse/catalog/ontology"
                ,req -> {
                    Route.of("postPage").write();
                    Ontology ontology = req.body(Ontology.class);
                    req.set(Ontology.class, ontology);
                    ContentResponse<Ontology> response = this.getController(app).addOntology(ontology);
                    return Results.with(response, response.status().getStatus());
                });

        /** get available ontologies*/
        app.get("/fuse/catalog/ontology"
                ,req -> {
                    ContentResponse<List<Ontology>> response = this.getController(app).getOntologies();
                    return Results.with(response, response.status().getStatus());
                });

        /** get the ontology by id */
        app.get("/fuse/catalog/ontology/:id"
                ,req -> {
                    ContentResponse response = this.getController(app).getOntology(req.param("id").value());
                    return Results.with(response, response.status().getStatus());
                });

        /** get the ontology by id */
        app.get("/fuse/catalog/ontology/:id/visualize",
                     (req,resp) -> API.dataView(app,req,resp,this));

        /** get available schemas **/
        app.get("/fuse/catalog/schema"
                ,req -> {
                    ContentResponse<List<String>> response = this.getController(app).getSchemas();
                    return Results.with(response, response.status().getStatus());
                });

        app.get("/fuse/catalog/schema/:id",
                req -> {
                    ContentResponse response = this.getController(app).getSchema(req.param("id").value());
                    return Results.with(response, response.status().getStatus());
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
        public static Object dataView(Jooby app, Request req, Response resp, CatalogControllerRegistrar controller) throws Throwable {

            Route.of("getCatalogOntologyVisualization").write();

            ContentResponse<Ontology> ontology = controller.getController(app).getOntology(req.param("id").value());
            String dotGraph = OntologyDescriptor.printGraph(ontology.getData());
            File file = SVGGraphRenderer.render(ontology.getData().getOnt(), dotGraph);
            ContentResponse<File> compose = ContentResponse.Builder.<File>builder(Status.OK, Status.NOT_FOUND)
                    .data(file)
                    .compose();
            return RegistrarsUtils.withImg(req,resp,compose);
        }


    }

    //endregion
}
