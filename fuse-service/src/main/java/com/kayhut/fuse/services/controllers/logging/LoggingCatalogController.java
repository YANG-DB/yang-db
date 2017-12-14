package com.kayhut.fuse.services.controllers.logging;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.kayhut.fuse.model.ontology.Ontology;
import com.kayhut.fuse.model.transport.ContentResponse;
import com.kayhut.fuse.services.controllers.CatalogController;
import com.kayhut.fuse.unipop.schemaProviders.GraphElementSchemaProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by roman.margolis on 14/12/2017.
 */
public class LoggingCatalogController implements CatalogController {
    public static final String injectionName = "LoggingCatalogController.inner";

    //region Constructors
    @Inject
    public LoggingCatalogController(@Named(injectionName)CatalogController controller) {
        this.logger = LoggerFactory.getLogger(this.getClass());
        this.controller = controller;
    }
    //endregion

    //region CatalogController Implementation
    @Override
    public ContentResponse<Ontology> getOntology(String id) {
        boolean thrownException = false;

        try {
            this.logger.debug("start getOntology");
            return controller.getOntology(id);
        } catch (Exception ex) {
            thrownException = true;
            this.logger.error("failed getOntology: {}", ex);
            return null;
        } finally {
            if (!thrownException) {
                this.logger.debug("finish getOntology");
            }
        }
    }

    @Override
    public ContentResponse<GraphElementSchemaProvider> getSchema(String id) {
        boolean thrownException = false;

        try {
            this.logger.debug("start getSchema");
            return controller.getSchema(id);
        } catch (Exception ex) {
            thrownException = true;
            this.logger.error("failed getSchema: {}", ex);
            return null;
        } finally {
            if (!thrownException) {
                this.logger.debug("finish getSchema");
            }
        }
    }
    //endregion

    //region Fields
    private Logger logger;
    private CatalogController controller;
    //endregion
}
