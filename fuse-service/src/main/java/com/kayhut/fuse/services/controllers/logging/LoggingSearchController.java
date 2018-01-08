package com.kayhut.fuse.services.controllers.logging;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.kayhut.fuse.logging.ElapsedConverter;
import com.kayhut.fuse.logging.RequestIdConverter;
import com.kayhut.fuse.services.suppliers.RequestIdSupplier;
import com.kayhut.fuse.model.transport.ContentResponse;
import com.kayhut.fuse.model.transport.CreateQueryRequest;
import com.kayhut.fuse.services.controllers.SearchController;
import org.slf4j.Logger;
import org.slf4j.MDC;

import static com.codahale.metrics.MetricRegistry.name;

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
            MetricRegistry metricRegistry) {
        this.controller = controller;
        this.logger = logger;
        this.requestIdSupplier = requestIdSupplier;
        this.metricRegistry = metricRegistry;
    }
    //endregion

    //region SearchController Implementation
    @Override
    public ContentResponse search(CreateQueryRequest request) {
        Timer.Context timerContext = this.metricRegistry.timer(name(this.logger.getName(), "search")).time();

        MDC.put(RequestIdConverter.key, this.requestIdSupplier.get());
        MDC.put(ElapsedConverter.key, Long.toString(System.currentTimeMillis()));
        boolean thrownException = false;

        try {
            this.logger.trace("start search");
            return controller.search(request);
        } catch (Exception ex) {
            thrownException = true;
            this.logger.error("failed search", ex);
            this.metricRegistry.meter(name(this.logger.getName(), "search", "failure")).mark();
            return null;
        } finally {
            if (!thrownException) {
                this.logger.trace("finish search");
                this.metricRegistry.meter(name(this.logger.getName(), "search", "success")).mark();
            }
            timerContext.stop();
        }
    }
    //endregion

    //region Fields
    private Logger logger;
    private RequestIdSupplier requestIdSupplier;
    private MetricRegistry metricRegistry;
    private SearchController controller;
    //endregion
}
