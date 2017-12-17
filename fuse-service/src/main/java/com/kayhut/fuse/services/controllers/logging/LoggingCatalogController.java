package com.kayhut.fuse.services.controllers.logging;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.kayhut.fuse.logging.ElapsedConverter;
import com.kayhut.fuse.model.ontology.Ontology;
import com.kayhut.fuse.model.transport.ContentResponse;
import com.kayhut.fuse.services.controllers.CatalogController;
import com.kayhut.fuse.unipop.schemaProviders.GraphElementSchemaProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

/**
 * Created by roman.margolis on 14/12/2017.
 */
public class LoggingCatalogController implements CatalogController {
    public static final String injectionName = "LoggingCatalogController.inner";

    //region Constructors
    @Inject
    public LoggingCatalogController(@Named(injectionName)CatalogController controller) {
        this.logger = LoggerFactory.getLogger(controller.getClass());
        this.controller = controller;
    }
    //endregion

    //region CatalogController Implementation
    @Override
    public ContentResponse<Ontology> getOntology(String id) {
        MDC.put(ElapsedConverter.key, Long.toString(System.currentTimeMillis()));
        boolean thrownException = false;

        try {
            this.logger.trace("start getOntology");
            return controller.getOntology(id);
        } catch (Exception ex) {
            thrownException = true;
            this.logger.error("failed getOntology: {}", ex);
            return null;
        } finally {
            if (!thrownException) {
                this.logger.trace("finish getOntology");
            }
        }
    }

    @Override
    public ContentResponse<GraphElementSchemaProvider> getSchema(String id) {
        MDC.put(ElapsedConverter.key, Long.toString(System.currentTimeMillis()));
        boolean thrownException = false;

        try {
            this.logger.trace("start getSchema");
            return controller.getSchema(id);
        } catch (Exception ex) {
            thrownException = true;
            this.logger.error("failed getSchema: {}", ex);
            return null;
        } finally {
            if (!thrownException) {
                this.logger.trace("finish getSchema");
            }
        }
    }
    //endregion

    //region Fields
    private Logger logger;
    private CatalogController controller;
    //endregion
}
