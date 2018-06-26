package com.kayhut.fuse.services.controllers.logging;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.kayhut.fuse.dispatcher.logging.*;
import com.kayhut.fuse.dispatcher.logging.LogMessage.MDCWriter.Composite;
import com.kayhut.fuse.logging.RequestExternalMetadata;
import com.kayhut.fuse.logging.RequestId;
import com.kayhut.fuse.services.suppliers.RequestExternalMetadataSupplier;
import com.kayhut.fuse.services.suppliers.RequestIdSupplier;
import com.kayhut.fuse.model.transport.ContentResponse;
import com.kayhut.fuse.model.transport.CreateQueryRequest;
import com.kayhut.fuse.services.controllers.SearchController;
import org.slf4j.Logger;

import java.util.concurrent.TimeUnit;

import static com.codahale.metrics.MetricRegistry.name;
import static com.kayhut.fuse.dispatcher.logging.LogMessage.Level.error;
import static com.kayhut.fuse.dispatcher.logging.LogMessage.Level.info;
import static com.kayhut.fuse.dispatcher.logging.LogMessage.Level.trace;
import static com.kayhut.fuse.dispatcher.logging.LogType.*;

/**
 * Created by roman.margolis on 14/12/2017.
 */
public class LoggingSearchController implements SearchController{
    public static final String controllerParameter = "LoggingSearchController.@controller";
    public static final String loggerParameter = "LoggingSearchController.@logger";

    //region Constructors
    @Inject
    public LoggingSearchController(
            @Named(controllerParameter) SearchController controller,
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

    //region SearchController Implementation
    @Override
    public ContentResponse search(CreateQueryRequest request) {
        Timer.Context timerContext = this.metricRegistry.timer(name(this.logger.getName(), search.toString())).time();

        Composite.of(Elapsed.now(), ElapsedFrom.now(),
                RequestId.of(this.requestIdSupplier.get()),
                RequestExternalMetadata.of(this.requestExternalMetadataSupplier.get()),
                RequestIdByScope.of(request.getId())).write();

        ContentResponse response = null;

        try {
            new LogMessage.Impl(this.logger, trace, "start search", sequence, LogType.of(start), search).log();
            response = this.controller.search(request);
            new LogMessage.Impl(this.logger, info, "finish search", sequence, LogType.of(success), search, ElapsedFrom.now()).log();
            new LogMessage.Impl(this.logger, trace, "finish search", sequence, LogType.of(success), search, ElapsedFrom.now()).log();
            this.metricRegistry.meter(name(this.logger.getName(), search.toString(), "success")).mark();
        } catch (Exception ex) {
            new LogMessage.Impl(this.logger, error, "failed search", sequence, LogType.of(failure), search, ElapsedFrom.now())
                    .with(ex).log();
            this.metricRegistry.meter(name(this.logger.getName(), search.toString(), "failure")).mark();
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
    private SearchController controller;

    private static MethodName.MDCWriter search = MethodName.of("search");
    private static LogMessage.MDCWriter sequence = Sequence.incr();
    //endregion
}
