package com.kayhut.fuse.services.controllers.logging;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import com.google.inject.Inject;
import com.google.inject.name.Named;
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
            this.logger.trace("start create");
            return controller.create(queryId, createCursorRequest);
        } catch (Exception ex) {
            thrownException = true;
            this.logger.error("failed create", ex);
            this.metricRegistry.meter(name(this.logger.getName(), "create", "failure")).mark();
            return null;
        } finally {
            if (!thrownException) {
                this.logger.trace("finish create");
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
            this.logger.trace("start getInfo");
            return controller.getInfo(queryId);
        } catch (Exception ex) {
            thrownException = true;
            this.logger.error("failed getInfo", ex);
            this.metricRegistry.meter(name(this.logger.getName(), "getInfoByQueryId", "failure")).mark();
            return null;
        } finally {
            if (!thrownException) {
                this.logger.trace("finish getInfo");
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
            this.logger.trace("start getInfo");
            return controller.getInfo(queryId, cursorId);
        } catch (Exception ex) {
            thrownException = true;
            this.logger.error("failed getInfo", ex);
            this.metricRegistry.meter(name(this.logger.getName(), "getInfoByQueryIdAndCursorId", "failure")).mark();
            return null;
        } finally {
            if (!thrownException) {
                this.logger.trace("finish getInfo");
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
            this.logger.trace("start delete");
            return controller.delete(queryId, cursorId);
        } catch (Exception ex) {
            thrownException = true;
            this.logger.error("failed delete", ex);
            this.metricRegistry.meter(name(this.logger.getName(), "delete", "failure")).mark();
            return null;
        } finally {
            if (!thrownException) {
                this.logger.trace("finish delete");
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
