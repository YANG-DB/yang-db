package com.kayhut.fuse.services.controllers.logging;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.kayhut.fuse.dispatcher.logging.LogMessage;
import com.kayhut.fuse.logging.ElapsedConverter;
import com.kayhut.fuse.logging.RequestIdConverter;
import com.kayhut.fuse.services.suppliers.RequestIdSupplier;
import com.kayhut.fuse.model.resourceInfo.CursorResourceInfo;
import com.kayhut.fuse.model.resourceInfo.StoreResourceInfo;
import com.kayhut.fuse.model.transport.ContentResponse;
import com.kayhut.fuse.model.transport.CreateCursorRequest;
import com.kayhut.fuse.services.controllers.CursorController;
import org.slf4j.Logger;
import org.slf4j.MDC;

import static com.codahale.metrics.MetricRegistry.name;
import static com.kayhut.fuse.dispatcher.logging.LogMessage.Level.error;
import static com.kayhut.fuse.dispatcher.logging.LogMessage.Level.info;
import static com.kayhut.fuse.dispatcher.logging.LogMessage.Level.trace;
import static com.kayhut.fuse.dispatcher.logging.LogMessage.LogType.finish;
import static com.kayhut.fuse.dispatcher.logging.LogMessage.LogType.start;

/**
 * Created by roman.margolis on 14/12/2017.
 */
public class LoggingCursorController implements CursorController {
    public static final String controllerParameter = "LoggingCursorController.@controller";
    public static final String loggerParameter = "LoggingCursorController.@logger";

    //region Constructors
    @Inject
    public LoggingCursorController(
            @Named(controllerParameter) CursorController controller,
            @Named(loggerParameter) Logger logger,
            RequestIdSupplier requestIdSupplier,
            MetricRegistry metricRegistry) {
        this.controller = controller;
        this.logger = logger;
        this.requestIdSupplier = requestIdSupplier;
        this.metricRegistry = metricRegistry;
    }
    //endregion

    //region CursorController Implementation
    @Override
    public ContentResponse<CursorResourceInfo> create(String queryId, CreateCursorRequest createCursorRequest) {
        Timer.Context timerContext = this.metricRegistry.timer(name(this.logger.getName(), "create")).time();

        MDC.put(RequestIdConverter.key, this.requestIdSupplier.get());
        MDC.put(ElapsedConverter.key, Long.toString(System.currentTimeMillis()));
        boolean thrownException = false;

        try {
            new LogMessage(this.logger, trace, start, "create", "start create").log();
            return controller.create(queryId, createCursorRequest);
        } catch (Exception ex) {
            thrownException = true;
            new LogMessage(this.logger, error, finish, "create", "failed create", ex).log();
            this.metricRegistry.meter(name(this.logger.getName(), "create", "failure")).mark();
            return null;
        } finally {
            if (!thrownException) {
                new LogMessage(this.logger, info, finish, "create", "finish create").log();
                new LogMessage(this.logger, trace, finish, "create", "finish create").log();
                this.metricRegistry.meter(name(this.logger.getName(), "create", "success")).mark();
            }
            timerContext.stop();
        }
    }

    @Override
    public ContentResponse<StoreResourceInfo> getInfo(String queryId) {
        Timer.Context timerContext = this.metricRegistry.timer(name(this.logger.getName(), "getInfoByQueryId")).time();

        MDC.put(RequestIdConverter.key, this.requestIdSupplier.get());
        MDC.put(ElapsedConverter.key, Long.toString(System.currentTimeMillis()));
        boolean thrownException = false;

        try {
            new LogMessage(this.logger, trace, start, "getInfoByQueryId", "start getInfoByQueryId").log();
            return controller.getInfo(queryId);
        } catch (Exception ex) {
            thrownException = true;
            new LogMessage(this.logger, error, finish, "getInfoByQueryId", "failed getInfoByQueryId", ex).log();
            this.metricRegistry.meter(name(this.logger.getName(), "getInfoByQueryId", "failure")).mark();
            return null;
        } finally {
            if (!thrownException) {
                new LogMessage(this.logger, info, finish, "getInfoByQueryId", "finish getInfoByQueryId").log();
                new LogMessage(this.logger, trace, finish, "getInfoByQueryId", "finish getInfoByQueryId").log();
                this.metricRegistry.meter(name(this.logger.getName(), "getInfoByQueryId", "success")).mark();
            }
            timerContext.stop();
        }
    }

    @Override
    public ContentResponse<CursorResourceInfo> getInfo(String queryId, String cursorId) {
        Timer.Context timerContext = this.metricRegistry.timer(name(this.logger.getName(), "getInfoByQueryIdAndCursorId")).time();

        MDC.put(RequestIdConverter.key, this.requestIdSupplier.get());
        MDC.put(ElapsedConverter.key, Long.toString(System.currentTimeMillis()));
        boolean thrownException = false;

        try {
            new LogMessage(this.logger, trace, start, "getInfoByQueryIdAndCursorId", "start getInfoByQueryIdAndCursorId").log();
            return controller.getInfo(queryId, cursorId);
        } catch (Exception ex) {
            thrownException = true;
            new LogMessage(this.logger, error, finish, "getInfoByQueryIdAndCursorId", "failed getInfoByQueryIdAndCursorId", ex).log();
            this.metricRegistry.meter(name(this.logger.getName(), "getInfoByQueryIdAndCursorId", "failure")).mark();
            return null;
        } finally {
            if (!thrownException) {
                new LogMessage(this.logger, info, finish, "getInfoByQueryIdAndCursorId", "finish getInfoByQueryIdAndCursorId").log();
                new LogMessage(this.logger, trace, finish, "getInfoByQueryIdAndCursorId", "finish getInfoByQueryIdAndCursorId").log();
                this.metricRegistry.meter(name(this.logger.getName(), "getInfoByQueryIdAndCursorId", "success")).mark();
            }
            timerContext.stop();
        }
    }

    @Override
    public ContentResponse<Boolean> delete(String queryId, String cursorId) {
        Timer.Context timerContext = this.metricRegistry.timer(name(this.logger.getName(), "delete")).time();

        MDC.put(RequestIdConverter.key, this.requestIdSupplier.get());
        MDC.put(ElapsedConverter.key, Long.toString(System.currentTimeMillis()));
        boolean thrownException = false;

        try {
            new LogMessage(this.logger, trace, start, "delete", "start delete").log();
            return controller.delete(queryId, cursorId);
        } catch (Exception ex) {
            thrownException = true;
            new LogMessage(this.logger, error, finish, "delete", "failed delete", ex).log();
            this.metricRegistry.meter(name(this.logger.getName(), "delete", "failure")).mark();
            return null;
        } finally {
            if (!thrownException) {
                new LogMessage(this.logger, info, finish, "delete", "finish delete").log();
                new LogMessage(this.logger, trace, finish, "delete", "finish delete").log();
                this.metricRegistry.meter(name(this.logger.getName(), "delete", "success")).mark();
            }
            timerContext.stop();
        }
    }
    //endregion

    //region Fields
    private Logger logger;
    private RequestIdSupplier requestIdSupplier;
    private MetricRegistry metricRegistry;
    private CursorController controller;
    //endregion
}
