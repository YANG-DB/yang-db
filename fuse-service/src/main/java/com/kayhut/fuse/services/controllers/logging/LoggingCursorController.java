package com.kayhut.fuse.services.controllers.logging;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.kayhut.fuse.dispatcher.logging.*;
import com.kayhut.fuse.logging.RequestId;
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
import static com.kayhut.fuse.dispatcher.logging.LogType.*;
import static com.kayhut.fuse.dispatcher.logging.RequestIdByScope.Builder.query;

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
        Timer.Context timerContext = this.metricRegistry.timer(name(this.logger.getName(), create.toString())).time();
        boolean thrownException = false;

        ContentResponse<CursorResourceInfo> response = null;
        try {
            new LogMessage.Impl(this.logger, trace, "start create", LogType.of(start),
                    create, RequestId.of(this.requestIdSupplier.get()), Elapsed.now(), ElapsedFrom.now()).log();
            response = controller.create(queryId, createCursorRequest);
            return response;
        } catch (Exception ex) {
            thrownException = true;
            new LogMessage.Impl(this.logger, error, "failed create", LogType.of(failure), create, ElapsedFrom.now())
                    .with(ex).log();
            this.metricRegistry.meter(name(this.logger.getName(), create.toString(), "failure")).mark();
            throw new RuntimeException(ex);
        } finally {
            if (!thrownException && response!=null) {
                new LogMessage.Impl(this.logger, info, "finish create",
                        RequestIdByScope.of(query(queryId)
                                .cursor(response.getData().getResourceId())
                                .get()),
                        LogType.of(success), create, ElapsedFrom.now()).log();
                new LogMessage.Impl(this.logger, trace, "finish create", LogType.of(success), create, ElapsedFrom.now()).log();
                this.metricRegistry.meter(name(this.logger.getName(), create.toString(), "success")).mark();
            }
            timerContext.stop();
        }
    }

    @Override
    public ContentResponse<StoreResourceInfo> getInfo(String queryId) {
        Timer.Context timerContext = this.metricRegistry.timer(name(this.logger.getName(), getInfoByQueryId.toString())).time();
        boolean thrownException = false;

        try {
            new LogMessage.Impl(this.logger, trace, "start getInfoByQueryId",
                    LogType.of(start), getInfoByQueryId,
                    RequestIdByScope.of(query(queryId).get()),
                    RequestId.of(this.requestIdSupplier.get()), Elapsed.now(), ElapsedFrom.now()).log();
            return controller.getInfo(queryId);
        } catch (Exception ex) {
            thrownException = true;
            new LogMessage.Impl(this.logger, error, "failed getInfoByQueryId", LogType.of(failure), getInfoByQueryId, ElapsedFrom.now())
                    .with(ex).log();
            this.metricRegistry.meter(name(this.logger.getName(), getInfoByQueryId.toString(), "failure")).mark();
            return null;
        } finally {
            if (!thrownException) {
                new LogMessage.Impl(this.logger, info, "finish getInfoByQueryId", LogType.of(success), getInfoByQueryId, ElapsedFrom.now()).log();
                new LogMessage.Impl(this.logger, trace, "finish getInfoByQueryId", LogType.of(success), getInfoByQueryId, ElapsedFrom.now()).log();
                this.metricRegistry.meter(name(this.logger.getName(), getInfoByQueryId.toString(), "success")).mark();
            }
            timerContext.stop();
        }
    }

    @Override
    public ContentResponse<CursorResourceInfo> getInfo(String queryId, String cursorId) {
        Timer.Context timerContext = this.metricRegistry.timer(name(this.logger.getName(), getInfoByQueryIdAndCursorId.toString())).time();
        boolean thrownException = false;

        try {
            new LogMessage.Impl(this.logger, trace, "start getInfoByQueryIdAndCursorId",
                    LogType.of(start), getInfoByQueryIdAndCursorId,
                    RequestIdByScope.of(query(queryId)
                            .cursor(cursorId)
                            .get()),
                    RequestId.of(this.requestIdSupplier.get()), Elapsed.now(), ElapsedFrom.now()).log();
            return controller.getInfo(queryId, cursorId);
        } catch (Exception ex) {
            thrownException = true;
            new LogMessage.Impl(this.logger, error, "failed getInfoByQueryIdAndCursorId", LogType.of(failure), getInfoByQueryIdAndCursorId, ElapsedFrom.now())
                    .with(ex).log();
            this.metricRegistry.meter(name(this.logger.getName(), getInfoByQueryIdAndCursorId.toString(), "failure")).mark();
            return null;
        } finally {
            if (!thrownException) {
                new LogMessage.Impl(this.logger, info, "finish getInfoByQueryIdAndCursorId", LogType.of(success), getInfoByQueryIdAndCursorId, ElapsedFrom.now()).log();
                new LogMessage.Impl(this.logger, trace, "finish getInfoByQueryIdAndCursorId", LogType.of(success), getInfoByQueryIdAndCursorId, ElapsedFrom.now()).log();
                this.metricRegistry.meter(name(this.logger.getName(), getInfoByQueryIdAndCursorId.toString(), "success")).mark();
            }
            timerContext.stop();
        }
    }

    @Override
    public ContentResponse<Boolean> delete(String queryId, String cursorId) {
        Timer.Context timerContext = this.metricRegistry.timer(name(this.logger.getName(), delete.toString())).time();
        boolean thrownException = false;

        try {
            new LogMessage.Impl(this.logger, trace, "start delete",
                    LogType.of(start), delete,
                    RequestIdByScope.of(query(queryId)
                            .cursor(cursorId)
                            .get()),
                    RequestId.of(this.requestIdSupplier.get()), Elapsed.now(), ElapsedFrom.now()).log();
            return controller.delete(queryId, cursorId);
        } catch (Exception ex) {
            thrownException = true;
            new LogMessage.Impl(this.logger, error, "failed delete", LogType.of(failure), delete, ElapsedFrom.now())
                    .with(ex).log();
            this.metricRegistry.meter(name(this.logger.getName(), delete.toString(), "failure")).mark();
            return null;
        } finally {
            if (!thrownException) {
                new LogMessage.Impl(this.logger, info, "finish delete", LogType.of(success), delete, ElapsedFrom.now()).log();
                new LogMessage.Impl(this.logger, trace, "finish delete", LogType.of(success), delete, ElapsedFrom.now()).log();
                this.metricRegistry.meter(name(this.logger.getName(), delete.toString(), "success")).mark();
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

    private static MethodName.MDCWriter create = MethodName.of("create");
    private static MethodName.MDCWriter getInfoByQueryId = MethodName.of("getInfoByQueryId");
    private static MethodName.MDCWriter getInfoByQueryIdAndCursorId = MethodName.of("getInfoByQueryIdAndCursorId");
    private static MethodName.MDCWriter delete = MethodName.of("delete");
    //endregion
}
