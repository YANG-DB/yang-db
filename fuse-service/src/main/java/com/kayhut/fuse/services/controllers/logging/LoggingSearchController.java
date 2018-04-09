package com.kayhut.fuse.services.controllers.logging;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.kayhut.fuse.dispatcher.logging.*;
import com.kayhut.fuse.dispatcher.logging.LogMessage.MDCWriter.Composite;
import com.kayhut.fuse.logging.ExternalRequestId;
import com.kayhut.fuse.logging.RequestId;
import com.kayhut.fuse.services.suppliers.ExternalRequestIdSupplier;
import com.kayhut.fuse.services.suppliers.RequestIdSupplier;
import com.kayhut.fuse.model.transport.ContentResponse;
import com.kayhut.fuse.model.transport.CreateQueryRequest;
import com.kayhut.fuse.services.controllers.SearchController;
import org.slf4j.Logger;
import org.slf4j.MDC;

import java.util.function.Supplier;

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
            ExternalRequestIdSupplier externalRequestIdSupplier,
            MetricRegistry metricRegistry) {
        this.controller = controller;
        this.logger = logger;
        this.requestIdSupplier = requestIdSupplier;
        this.externalRequestIdSupplier = externalRequestIdSupplier;
        this.metricRegistry = metricRegistry;
    }
    //endregion

    //region SearchController Implementation
    @Override
    public ContentResponse search(CreateQueryRequest request) {
        Timer.Context timerContext = this.metricRegistry.timer(name(this.logger.getName(), search.toString())).time();
        boolean thrownException = false;

        Composite.of(Elapsed.now(), ElapsedFrom.now(),
                RequestId.of(this.requestIdSupplier.get()),
                ExternalRequestId.of(this.externalRequestIdSupplier.get()),
                RequestIdByScope.of(request.getId())).write();

        try {
            new LogMessage.Impl(this.logger, trace, "start search", LogType.of(start), search).log();
            return controller.search(request);
        } catch (Exception ex) {
            thrownException = true;
            new LogMessage.Impl(this.logger, error, "failed search", LogType.of(failure), search, ElapsedFrom.now())
                    .with(ex).log();
            this.metricRegistry.meter(name(this.logger.getName(), search.toString(), "failure")).mark();
            throw new RuntimeException(ex);
        } finally {
            if (!thrownException) {
                new LogMessage.Impl(this.logger, info, "finish search", LogType.of(success), search, ElapsedFrom.now()).log();
                new LogMessage.Impl(this.logger, trace, "finish search", LogType.of(success), search, ElapsedFrom.now()).log();
                this.metricRegistry.meter(name(this.logger.getName(), search.toString(), "success")).mark();
            }
            timerContext.stop();
        }
    }
    //endregion

    //region Fields
    private Logger logger;
    private RequestIdSupplier requestIdSupplier;
    private Supplier<String> externalRequestIdSupplier;
    private MetricRegistry metricRegistry;
    private SearchController controller;

    private static MethodName.MDCWriter search = MethodName.of("search");
    //endregion
}
