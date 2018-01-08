package com.kayhut.fuse.services.controllers.logging;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.kayhut.fuse.logging.ElapsedConverter;
import com.kayhut.fuse.logging.RequestIdConverter;
import com.kayhut.fuse.services.suppliers.RequestIdSupplier;
import com.kayhut.fuse.model.resourceInfo.PageResourceInfo;
import com.kayhut.fuse.model.resourceInfo.StoreResourceInfo;
import com.kayhut.fuse.model.transport.ContentResponse;
import com.kayhut.fuse.model.transport.CreatePageRequest;
import com.kayhut.fuse.services.controllers.PageController;
import org.slf4j.Logger;
import org.slf4j.MDC;

import static com.codahale.metrics.MetricRegistry.name;

/**
 * Created by roman.margolis on 14/12/2017.
 */
public class LoggingPageController implements PageController {
    public static final String controllerParameter = "LoggingPageController.@controller";
    public static final String loggerParameter = "LoggingPageController.@logger";

    //region Constructors
    @Inject
    public LoggingPageController(
            @Named(controllerParameter) PageController controller,
            @Named(loggerParameter) Logger logger,
            RequestIdSupplier requestIdSupplier,
            MetricRegistry metricRegistry) {
        this.controller = controller;
        this.logger = logger;
        this.requestIdSupplier = requestIdSupplier;
        this.metricRegistry = metricRegistry;
    }
    //endregion

    //region PageController Implementation
    @Override
    public ContentResponse<PageResourceInfo> create(String queryId, String cursorId, CreatePageRequest createPageRequest) {
        Timer.Context timerContext = this.metricRegistry.timer(name(this.logger.getName(), "create")).time();

        MDC.put(RequestIdConverter.key, this.requestIdSupplier.get());
        MDC.put(ElapsedConverter.key, Long.toString(System.currentTimeMillis()));
        boolean thrownException = false;

        try {
            this.logger.trace("start create");
            return controller.create(queryId, cursorId, createPageRequest);
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
    public ContentResponse<StoreResourceInfo> getInfo(String queryId, String cursorId) {
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
    public ContentResponse<PageResourceInfo> getInfo(String queryId, String cursorId, String pageId) {
        Timer.Context timerContext = this.metricRegistry.timer(name(this.logger.getName(), "getInfoByQueryIdAndCursorIdAndPageId")).time();

        MDC.put(RequestIdConverter.key, this.requestIdSupplier.get());
        MDC.put(ElapsedConverter.key, Long.toString(System.currentTimeMillis()));
        boolean thrownException = false;

        try {
            this.logger.trace("start getInfo");
            return controller.getInfo(queryId, cursorId, pageId);
        } catch (Exception ex) {
            thrownException = true;
            this.logger.error("failed getInfo", ex);
            this.metricRegistry.meter(name(this.logger.getName(), "getInfoByQueryIdAndCursorIdAndPageId", "failure")).mark();
            return null;
        } finally {
            if (!thrownException) {
                this.logger.trace("finish getInfo");
                this.metricRegistry.meter(name(this.logger.getName(), "getInfoByQueryIdAndCursorIdAndPageId", "success")).mark();
            }
            timerContext.stop();
        }
    }

    @Override
    public ContentResponse<Object> getData(String queryId, String cursorId, String pageId) {
        Timer.Context timerContext = this.metricRegistry.timer(name(this.logger.getName(), "getData")).time();

        MDC.put(RequestIdConverter.key, this.requestIdSupplier.get());
        MDC.put(ElapsedConverter.key, Long.toString(System.currentTimeMillis()));
        boolean thrownException = false;

        try {
            this.logger.trace("start getData");
            return controller.getData(queryId, cursorId, pageId);
        } catch (Exception ex) {
            thrownException = true;
            this.logger.error("failed getData", ex);
            this.metricRegistry.meter(name(this.logger.getName(), "getData", "failure")).mark();
            return null;
        } finally {
            if (!thrownException) {
                this.logger.trace("finish getData");
                this.metricRegistry.meter(name(this.logger.getName(), "getData", "success")).mark();
            }
            timerContext.stop();
        }
    }
    //endregion

    //region Fields
    private Logger logger;
    private RequestIdSupplier requestIdSupplier;
    private MetricRegistry metricRegistry;
    private PageController controller;
    //endregion
}
