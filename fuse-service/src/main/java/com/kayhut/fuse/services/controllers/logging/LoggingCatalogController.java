package com.kayhut.fuse.services.controllers.logging;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.kayhut.fuse.dispatcher.logging.*;
import com.kayhut.fuse.logging.RequestExternalMetadata;
import com.kayhut.fuse.logging.RequestId;
import com.kayhut.fuse.services.suppliers.RequestExternalMetadataSupplier;
import com.kayhut.fuse.services.suppliers.RequestIdSupplier;
import com.kayhut.fuse.model.ontology.Ontology;
import com.kayhut.fuse.model.transport.ContentResponse;
import com.kayhut.fuse.services.controllers.CatalogController;
import com.kayhut.fuse.unipop.schemaProviders.GraphElementSchemaProvider;
import org.slf4j.Logger;

import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.codahale.metrics.MetricRegistry.name;
import static com.kayhut.fuse.dispatcher.logging.LogMessage.Level.error;
import static com.kayhut.fuse.dispatcher.logging.LogMessage.Level.info;
import static com.kayhut.fuse.dispatcher.logging.LogMessage.Level.trace;
import static com.kayhut.fuse.dispatcher.logging.LogType.*;

/**
 * Created by roman.margolis on 14/12/2017.
 */
public class LoggingCatalogController implements CatalogController {
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
        this.controller = controller;
        this.logger = logger;
        this.requestIdSupplier = requestIdSupplier;
        this.requestExternalMetadataSupplier = requestExternalMetadataSupplier;
        this.metricRegistry = metricRegistry;
    }
    //endregion

    //region CatalogController Implementation
    @Override
    public ContentResponse<Ontology> getOntology(String id) {
        Timer.Context timerContext = this.metricRegistry.timer(name(this.logger.getName(), getOntology.toString())).time();

        LogMessage.MDCWriter.Composite.of(Elapsed.now(), ElapsedFrom.now(),
                RequestId.of(this.requestIdSupplier.get()),
                RequestExternalMetadata.of(this.requestExternalMetadataSupplier.get())).write();

        ContentResponse<Ontology> response = null;

        try {
            new LogMessage.Impl(this.logger, trace, "start getOntology", LogType.of(start), getOntology).log();
            response = this.controller.getOntology(id);
            new LogMessage.Impl(this.logger, info, "finish getOntology", LogType.of(success), getOntology, ElapsedFrom.now()).log();
            new LogMessage.Impl(this.logger, trace, "finish getOntology", LogType.of(success), getOntology, ElapsedFrom.now()).log();
            this.metricRegistry.meter(name(this.logger.getName(), getOntology.toString(), "success")).mark();
        } catch (Exception ex) {
            new LogMessage.Impl(this.logger, error, "failed getOntology", LogType.of(failure), getOntology, ElapsedFrom.now())
                    .with(ex).log();
            this.metricRegistry.meter(name(this.logger.getName(), getOntology.toString(), "failure")).mark();
            response = ContentResponse.internalError(ex);
        }

        return ContentResponse.Builder.builder(response)
                .requestId(this.requestIdSupplier.get())
                .external(this.requestExternalMetadataSupplier.get())
                .elapsed(TimeUnit.MILLISECONDS.convert(timerContext.stop(), TimeUnit.NANOSECONDS))
                .compose();
    }

    @Override
    public ContentResponse<List<Ontology>> getOntologies() {
        Timer.Context timerContext = this.metricRegistry.timer(name(this.logger.getName(), getOntology.toString())).time();

        LogMessage.MDCWriter.Composite.of(Elapsed.now(), ElapsedFrom.now(),
                RequestId.of(this.requestIdSupplier.get()),
                RequestExternalMetadata.of(this.requestExternalMetadataSupplier.get())).write();

        ContentResponse<List<Ontology>> response = null;

        try {
            new LogMessage.Impl(this.logger, trace, "start getOntology", LogType.of(start), getOntology).log();
            response = this.controller.getOntologies();
            new LogMessage.Impl(this.logger, info, "finish getOntology", LogType.of(success), getOntology, ElapsedFrom.now()).log();
            new LogMessage.Impl(this.logger, trace, "finish getOntology", LogType.of(success), getOntology, ElapsedFrom.now()).log();
            this.metricRegistry.meter(name(this.logger.getName(), getOntology.toString(), "success")).mark();
        } catch (Exception ex) {
            new LogMessage.Impl(this.logger, error, "failed getOntology", LogType.of(failure), getOntology, ElapsedFrom.now())
                    .with(ex).log();
            this.metricRegistry.meter(name(this.logger.getName(), getOntology.toString(), "failure")).mark();
            response = ContentResponse.internalError(ex);
        }

        return ContentResponse.Builder.builder(response)
                .requestId(this.requestIdSupplier.get())
                .external(this.requestExternalMetadataSupplier.get())
                .elapsed(TimeUnit.MILLISECONDS.convert(timerContext.stop(), TimeUnit.NANOSECONDS))
                .compose();
    }

    @Override
    public ContentResponse<GraphElementSchemaProvider> getSchema(String id) {
        Timer.Context timerContext = this.metricRegistry.timer(name(this.logger.getName(), getSchema.toString())).time();

        LogMessage.MDCWriter.Composite.of(Elapsed.now(), ElapsedFrom.now(),
                RequestId.of(this.requestIdSupplier.get()),
                RequestExternalMetadata.of(this.requestExternalMetadataSupplier.get())).write();

        ContentResponse<GraphElementSchemaProvider> response = null;

        try {
            new LogMessage.Impl(this.logger, trace, "start getSchema", LogType.of(start), getSchema).log();
            response = this.controller.getSchema(id);
            new LogMessage.Impl(this.logger, info, "finish getSchema", LogType.of(success), getSchema, ElapsedFrom.now()).log();
            new LogMessage.Impl(this.logger, trace, "finish getSchema", LogType.of(success), getSchema, ElapsedFrom.now()).log();
            this.metricRegistry.meter(name(this.logger.getName(), getSchema.toString(), "success")).mark();
        } catch (Exception ex) {
            new LogMessage.Impl(this.logger, error, "failed getSchema", LogType.of(failure), getSchema, ElapsedFrom.now())
                    .with(ex).log();
            this.metricRegistry.meter(name(this.logger.getName(), getSchema.toString(), "failure")).mark();
            response = ContentResponse.internalError(ex);
        }

        return ContentResponse.Builder.builder(response)
                .requestId(this.requestIdSupplier.get())
                .external(this.requestExternalMetadataSupplier.get())
                .elapsed(TimeUnit.MILLISECONDS.convert(timerContext.stop(), TimeUnit.NANOSECONDS))
                .compose();
    }

    @Override
    public ContentResponse<List<GraphElementSchemaProvider>> getSchemas() {
        Timer.Context timerContext = this.metricRegistry.timer(name(this.logger.getName(), getSchema.toString())).time();

        LogMessage.MDCWriter.Composite.of(Elapsed.now(), ElapsedFrom.now(),
                RequestId.of(this.requestIdSupplier.get()),
                RequestExternalMetadata.of(this.requestExternalMetadataSupplier.get())).write();

        ContentResponse<List<GraphElementSchemaProvider>> response = null;

        try {
            new LogMessage.Impl(this.logger, trace, "start getSchema", LogType.of(start), getSchema).log();
            response = this.controller.getSchemas();
            new LogMessage.Impl(this.logger, info, "finish getSchema", LogType.of(success), getSchema, ElapsedFrom.now()).log();
            new LogMessage.Impl(this.logger, trace, "finish getSchema", LogType.of(success), getSchema, ElapsedFrom.now()).log();
            this.metricRegistry.meter(name(this.logger.getName(), getSchema.toString(), "success")).mark();
        } catch (Exception ex) {
            new LogMessage.Impl(this.logger, error, "failed getSchema", LogType.of(failure), getSchema, ElapsedFrom.now())
                    .with(ex).log();
            this.metricRegistry.meter(name(this.logger.getName(), getSchema.toString(), "failure")).mark();
            response = ContentResponse.internalError(ex);
        }

        return ContentResponse.Builder.builder(response)
                .requestId(this.requestIdSupplier.get())
                .external(this.requestExternalMetadataSupplier.get())
                .elapsed(TimeUnit.MILLISECONDS.convert(timerContext.stop(), TimeUnit.NANOSECONDS))
                .compose();
    }
    //endregion

    //region Fields
    private Logger logger;
    private RequestIdSupplier requestIdSupplier;
    private RequestExternalMetadataSupplier requestExternalMetadataSupplier;
    private MetricRegistry metricRegistry;
    private CatalogController controller;

    private static MethodName.MDCWriter getOntology = MethodName.of("getOntology");
    private static MethodName.MDCWriter getSchema = MethodName.of("getSchema");
    //endregion
}
