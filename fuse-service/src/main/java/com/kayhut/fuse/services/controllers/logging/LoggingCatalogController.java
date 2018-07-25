package com.kayhut.fuse.services.controllers.logging;

import com.codahale.metrics.MetricRegistry;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.kayhut.fuse.dispatcher.logging.*;
import com.kayhut.fuse.services.suppliers.RequestExternalMetadataSupplier;
import com.kayhut.fuse.services.suppliers.RequestIdSupplier;
import com.kayhut.fuse.model.ontology.Ontology;
import com.kayhut.fuse.model.transport.ContentResponse;
import com.kayhut.fuse.services.controllers.CatalogController;
import com.kayhut.fuse.unipop.schemaProviders.GraphElementSchemaProvider;
import org.slf4j.Logger;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static com.kayhut.fuse.dispatcher.logging.LogMessage.Level.info;
import static com.kayhut.fuse.dispatcher.logging.LogMessage.Level.trace;

/**
 * Created by roman.margolis on 14/12/2017.
 */
public class LoggingCatalogController extends LoggingControllerBase<CatalogController> implements CatalogController {
    public static final String controllerParameter = "LoggingCatalogController.@controller";
    public static final String loggerParameter = "LoggingCatalogController.@logger";

    //region Constructors
    @Inject
    public LoggingCatalogController(
            @Named(controllerParameter) CatalogController controller,
            @Named(loggerParameter) Logger logger,
            RequestIdSupplier requestIdSupplier,
            RequestExternalMetadataSupplier requestExternalMetadataSupplier,
            MetricRegistry metricRegistry) {
        super(controller, logger, requestIdSupplier, requestExternalMetadataSupplier, metricRegistry);
    }
    //endregion

    //region CatalogController Implementation
    @Override
    public ContentResponse<Ontology> getOntology(String id) {
        return new LoggingSyncMethodDecorator<ContentResponse<Ontology>>(
                this.logger,
                this.metricRegistry,
                getOntology,
                this.primerMdcWriter(),
                Collections.singletonList(trace),
                Arrays.asList(info, trace))
                .decorate(() -> this.controller.getOntology(id), this.resultHandler());
    }

    @Override
    public ContentResponse<List<Ontology>> getOntologies() {
        return new LoggingSyncMethodDecorator<ContentResponse<List<Ontology>>>(
                this.logger,
                this.metricRegistry,
                getOntologies,
                this.primerMdcWriter(),
                Collections.singletonList(trace),
                Arrays.asList(info, trace))
                .decorate(() -> this.controller.getOntologies(), this.resultHandler());
    }

    @Override
    public ContentResponse<GraphElementSchemaProvider> getSchema(String id) {
        return new LoggingSyncMethodDecorator<ContentResponse<GraphElementSchemaProvider>>(
                this.logger,
                this.metricRegistry,
                getSchema,
                this.primerMdcWriter(),
                Collections.singletonList(trace),
                Arrays.asList(info, trace))
                .decorate(() -> this.controller.getSchema(id), this.resultHandler());
    }

    @Override
    public ContentResponse<List<GraphElementSchemaProvider>> getSchemas() {
        return new LoggingSyncMethodDecorator<ContentResponse<List<GraphElementSchemaProvider>>>(
                this.logger,
                this.metricRegistry,
                getSchemas,
                this.primerMdcWriter(),
                Collections.singletonList(trace),
                Arrays.asList(info, trace))
                .decorate(() -> this.controller.getSchemas(), this.resultHandler());
    }
    //endregion

    //region Fields
    private static MethodName.MDCWriter getOntology = MethodName.of("getOntology");
    private static MethodName.MDCWriter getOntologies = MethodName.of("getOntologies");
    private static MethodName.MDCWriter getSchema = MethodName.of("getSchema");
    private static MethodName.MDCWriter getSchemas = MethodName.of("getSchemas");
    //endregion
}
