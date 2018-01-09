package com.kayhut.fuse.services.controllers.logging;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.kayhut.fuse.dispatcher.logging.LogMessage;
import com.kayhut.fuse.logging.ElapsedConverter;
import com.kayhut.fuse.logging.RequestIdConverter;
import com.kayhut.fuse.services.suppliers.RequestIdSupplier;
import com.kayhut.fuse.model.resourceInfo.FuseResourceInfo;
import com.kayhut.fuse.model.transport.ContentResponse;
import com.kayhut.fuse.services.controllers.ApiDescriptionController;
import org.slf4j.Logger;
import org.slf4j.MDC;

import static com.codahale.metrics.MetricRegistry.name;
import static com.kayhut.fuse.dispatcher.logging.LogMessage.Level.*;
import static com.kayhut.fuse.dispatcher.logging.LogMessage.LogType.*;

/**
 * Created by roman.margolis on 14/12/2017.
 */
public class LoggingApiDescriptionController implements ApiDescriptionController {
    public static final String controllerParameter = "LoggingApiDescriptionController.@controller";
    public static final String loggerParameter = "LoggingApiDescriptionController.@logger";

    //region Constructors
    @Inject
    public LoggingApiDescriptionController(
            @Named(controllerParameter) ApiDescriptionController controller,
            @Named(loggerParameter) Logger logger,
            RequestIdSupplier requestIdSupplier,
            MetricRegistry metricRegistry) {
        this.controller = controller;
        this.logger = logger;
        this.requestIdSupplier = requestIdSupplier;
        this.metricRegistry = metricRegistry;
    }
    //endregion

    //region ApiDescriptionController Implementation
    @Override
    public ContentResponse<FuseResourceInfo> getInfo() {
        Timer.Context timerContext = this.metricRegistry.timer(name(this.logger.getName(), "getInfo")).time();

        MDC.put(RequestIdConverter.key, this.requestIdSupplier.get());
        MDC.put(ElapsedConverter.key, Long.toString(System.currentTimeMillis()));
        boolean thrownException = false;

        try {
            new LogMessage(this.logger, trace, start, "getInfo", "start getInfo").log();
            return controller.getInfo();
        } catch (Exception ex) {
            thrownException = true;
            new LogMessage(this.logger, error, finish, "getInfo", "failed getInfo", ex).log();
            this.metricRegistry.meter(name(this.logger.getName(), "getInfo", "failure")).mark();
            return null;
        } finally {
            if (!thrownException) {
                new LogMessage(this.logger, info, finish, "getInfo", "finish getInfo").log();
                new LogMessage(this.logger, trace, finish, "getInfo", "finish getInfo").log();
                this.metricRegistry.meter(name(this.logger.getName(), "getInfo", "success")).mark();
            }
            timerContext.stop();
        }
    }
    //endregion

    //region Fields
    private Logger logger;
    private RequestIdSupplier requestIdSupplier;
    private MetricRegistry metricRegistry;
    private ApiDescriptionController controller;
    //endregion
}
