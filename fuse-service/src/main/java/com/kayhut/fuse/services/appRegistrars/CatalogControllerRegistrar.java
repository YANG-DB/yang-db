package com.kayhut.fuse.services.appRegistrars;

import com.kayhut.fuse.dispatcher.urlSupplier.AppUrlSupplier;
import com.kayhut.fuse.model.ontology.Ontology;
import com.kayhut.fuse.model.transport.ContentResponse;
import com.kayhut.fuse.services.controllers.CatalogController;
import com.kayhut.fuse.unipop.schemaProviders.GraphElementSchemaProvider;
import org.jooby.Jooby;
import org.jooby.Results;
import org.jooby.Status;

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
        app.use("/fuse/catalog/ontology")
                .get(req -> {
                    ContentResponse<List<Ontology>> response = this.getController(app).getOntologies();
                    return Results.with(response, response.status());
                });

        /** get the ontology by id */
        app.use("/fuse/catalog/ontology/:id")
                .get(req -> {
                    ContentResponse response = this.getController(app).getOntology(req.param("id").value());
                    return Results.with(response, response.status());
                });

        /** get available schemas **/
        app.use("/fuse/catalog/schema")
                .get(req -> {
                    ContentResponse<List<GraphElementSchemaProvider>> response = this.getController(app).getSchemas();
                    return Results.with(response, response.status());
                });

        app.use("/fuse/catalog/schema/:id")
                .get(req -> {
                    ContentResponse response = this.getController(app).getSchema(req.param("id").value());
                    return Results.with(response, response.status());
                });
    }
    //endregion
}
