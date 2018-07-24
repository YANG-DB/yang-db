package com.kayhut.fuse.services.controllers.logging;

import com.codahale.metrics.MetricRegistry;
import com.google.inject.name.Named;
import com.kayhut.fuse.dispatcher.decorators.MethodDecorator;
import com.kayhut.fuse.dispatcher.logging.Elapsed;
import com.kayhut.fuse.dispatcher.logging.LogMessage;
import com.kayhut.fuse.dispatcher.logging.RequestIdByScope;
import com.kayhut.fuse.logging.RequestExternalMetadata;
import com.kayhut.fuse.logging.RequestId;
import com.kayhut.fuse.model.transport.ContentResponse;
import com.kayhut.fuse.services.controllers.PageController;
import com.kayhut.fuse.services.suppliers.RequestExternalMetadataSupplier;
import com.kayhut.fuse.services.suppliers.RequestIdSupplier;
import org.slf4j.Logger;

import static com.kayhut.fuse.dispatcher.logging.RequestIdByScope.Builder.query;

public abstract class LoggingControllerBase<TController> {
    //region Constructors
    public LoggingControllerBase(
            TController controller,
            Logger logger,
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

    //region Protected Methods
    protected LogMessage.MDCWriter primerMdcWriter() {
        return LogMessage.MDCWriter.Composite.of(
                Elapsed.now(),
                RequestId.of(this.requestIdSupplier.get()),
                RequestExternalMetadata.of(this.requestExternalMetadataSupplier.get()));
    }

    protected <TResult> MethodDecorator.ResultHandler<ContentResponse<TResult>, Long> resultHandler() {
        return new MethodDecorator.ResultHandler<ContentResponse<TResult>, Long>() {
            @Override
            public ContentResponse<TResult> onSuccess(ContentResponse<TResult> response, Long elapsed) {
                return ContentResponse.Builder.builder(response)
                        .requestId(requestIdSupplier.get())
                        .external(requestExternalMetadataSupplier.get())
                        .elapsed(elapsed)
                        .compose();
            }

            @Override
            public ContentResponse<TResult> onFailure(Exception ex, Long elapsed) {
                return onSuccess(ContentResponse.internalError(ex), elapsed);
            }
        };
    }
    //endregion

    //region Fields
    protected TController controller;
    protected Logger logger;
    protected RequestIdSupplier requestIdSupplier;
    protected RequestExternalMetadataSupplier requestExternalMetadataSupplier;
    protected MetricRegistry metricRegistry;
    //endregion
}
