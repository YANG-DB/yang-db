package com.kayhut.fuse.services.controllers.logging;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.kayhut.fuse.dispatcher.logging.*;
import com.kayhut.fuse.dispatcher.logging.LogMessage.MDCWriter.Composite;
import com.kayhut.fuse.logging.ExternalRequestId;
import com.kayhut.fuse.logging.RequestId;
import com.kayhut.fuse.model.resourceInfo.PageResourceInfo;
import com.kayhut.fuse.model.resourceInfo.StoreResourceInfo;
import com.kayhut.fuse.model.transport.ContentResponse;
import com.kayhut.fuse.model.transport.CreatePageRequest;
import com.kayhut.fuse.services.controllers.PageController;
import com.kayhut.fuse.services.suppliers.ExternalRequestIdSupplier;
import com.kayhut.fuse.services.suppliers.RequestIdSupplier;
import org.slf4j.Logger;

import java.util.function.Supplier;

import static com.codahale.metrics.MetricRegistry.name;
import static com.kayhut.fuse.dispatcher.logging.LogMessage.Level.*;
import static com.kayhut.fuse.dispatcher.logging.LogType.*;
import static com.kayhut.fuse.dispatcher.logging.RequestIdByScope.Builder.query;

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
            ExternalRequestIdSupplier externalRequestIdSupplier,
            MetricRegistry metricRegistry) {
        this.controller = controller;
        this.logger = logger;
        this.requestIdSupplier = requestIdSupplier;
        this.externalRequestIdSupplier = externalRequestIdSupplier;

        this.metricRegistry = metricRegistry;
    }
    //endregion

    //region PageController Implementation
    @Override
    public ContentResponse<PageResourceInfo> create(String queryId, String cursorId, CreatePageRequest createPageRequest) {
        Timer.Context timerContext = this.metricRegistry.timer(name(this.logger.getName(), create.toString())).time();
        boolean thrownException = false;

        Composite.of(Elapsed.now(), ElapsedFrom.now(),
                RequestId.of(this.requestIdSupplier.get()),
                ExternalRequestId.of(this.externalRequestIdSupplier.get()),
                RequestIdByScope.of(query(queryId).cursor(cursorId).get())).write();

        try {
            new LogMessage.Impl(this.logger, trace, "start create", LogType.of(start), create).log();
            return controller.create(queryId, cursorId, createPageRequest);
        } catch (Exception ex) {
            thrownException = true;
            new LogMessage.Impl(this.logger, error, "failed create", LogType.of(failure), create, ElapsedFrom.now())
                    .with(ex).log();
            this.metricRegistry.meter(name(this.logger.getName(), create.toString(), "failure")).mark();
            throw new RuntimeException(ex);
        } finally {
            if (!thrownException) {
                new LogMessage.Impl(this.logger, info, "finish create",
                        LogType.of(success), create, ElapsedFrom.now()).log();
                new LogMessage.Impl(this.logger, trace, "finish create", LogType.of(success), create, ElapsedFrom.now()).log();
                this.metricRegistry.meter(name(this.logger.getName(), create.toString(), "success")).mark();
            }
            timerContext.stop();
        }
    }

    @Override
    public ContentResponse<StoreResourceInfo> getInfo(String queryId, String cursorId) {
        Timer.Context timerContext = this.metricRegistry.timer(name(this.logger.getName(), getInfoByQueryIdAndCursorId.toString())).time();
        boolean thrownException = false;

        Composite.of(Elapsed.now(), ElapsedFrom.now(),
                RequestId.of(this.requestIdSupplier.get()),
                ExternalRequestId.of(this.externalRequestIdSupplier.get()),
                RequestIdByScope.of(query(queryId).cursor(cursorId).get())).write();

        try {
            new LogMessage.Impl(this.logger, trace, "start getInfoByQueryIdAndCursorId", LogType.of(start), getInfoByQueryIdAndCursorId).log();
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
    public ContentResponse<PageResourceInfo> getInfo(String queryId, String cursorId, String pageId) {
        Timer.Context timerContext = this.metricRegistry.timer(name(this.logger.getName(), getInfoByQueryIdAndCursorIdAndPageId.toString())).time();
        boolean thrownException = false;

        Composite.of(Elapsed.now(), ElapsedFrom.now(),
                RequestId.of(this.requestIdSupplier.get()),
                ExternalRequestId.of(this.externalRequestIdSupplier.get()),
                RequestIdByScope.of(query(queryId).cursor(cursorId).page(pageId).get())).write();

        try {
            new LogMessage.Impl(this.logger, trace, "start getInfoByQueryIdAndCursorIdAndPageId", LogType.of(start), getInfoByQueryIdAndCursorIdAndPageId).log();
            return controller.getInfo(queryId, cursorId, pageId);
        } catch (Exception ex) {
            thrownException = true;
            new LogMessage.Impl(this.logger, error, "failed getInfoByQueryIdAndCursorIdAndPageId", LogType.of(failure), getInfoByQueryIdAndCursorIdAndPageId, ElapsedFrom.now())
                    .with(ex).log();
            this.metricRegistry.meter(name(this.logger.getName(), getInfoByQueryIdAndCursorIdAndPageId.toString(), "failure")).mark();
            return null;
        } finally {
            if (!thrownException) {
                new LogMessage.Impl(this.logger, info, "finish getInfoByQueryIdAndCursorIdAndPageId", LogType.of(success), getInfoByQueryIdAndCursorIdAndPageId, ElapsedFrom.now()).log();
                new LogMessage.Impl(this.logger, trace, "finish getInfoByQueryIdAndCursorIdAndPageId", LogType.of(success), getInfoByQueryIdAndCursorIdAndPageId, ElapsedFrom.now()).log();
                this.metricRegistry.meter(name(this.logger.getName(), getInfoByQueryIdAndCursorIdAndPageId.toString(), "success")).mark();
            }
            timerContext.stop();
        }
    }

    @Override
    public ContentResponse<Object> getData(String queryId, String cursorId, String pageId) {
        Timer.Context timerContext = this.metricRegistry.timer(name(this.logger.getName(), getData.toString())).time();
        boolean thrownException = false;

        Composite.of(Elapsed.now(), ElapsedFrom.now(),
                RequestId.of(this.requestIdSupplier.get()),
                ExternalRequestId.of(this.externalRequestIdSupplier.get()),
                RequestIdByScope.of(query(queryId).cursor(cursorId).page(pageId).get())).write();

        try {
            new LogMessage.Impl(this.logger, trace, "start getData", LogType.of(start), getData).log();
            return controller.getData(queryId, cursorId, pageId);
        } catch (Exception ex) {
            thrownException = true;
            new LogMessage.Impl(this.logger, error, "failed getData", LogType.of(failure), getData, ElapsedFrom.now())
                    .with(ex).log();
            this.metricRegistry.meter(name(this.logger.getName(), getData.toString(), "failure")).mark();
            return null;
        } finally {
            if (!thrownException) {
                new LogMessage.Impl(this.logger, info, "finish getData", LogType.of(success), getData, ElapsedFrom.now()).log();
                new LogMessage.Impl(this.logger, trace, "finish getData", LogType.of(success), getData, ElapsedFrom.now()).log();
                this.metricRegistry.meter(name(this.logger.getName(), getData.toString(), "success")).mark();
            }
            timerContext.stop();
        }
    }
    //endregion

    //region Fields
    private Logger logger;
    private RequestIdSupplier requestIdSupplier;
    private ExternalRequestIdSupplier externalRequestIdSupplier;
    private MetricRegistry metricRegistry;
    private PageController controller;

    private static MethodName.MDCWriter create = MethodName.of("create");
    private static MethodName.MDCWriter getInfoByQueryIdAndCursorId = MethodName.of("getInfoByQueryIdAndCursorId");
    private static MethodName.MDCWriter getInfoByQueryIdAndCursorIdAndPageId = MethodName.of("getInfoByQueryIdAndCursorIdAndPageId");
    private static MethodName.MDCWriter getData = MethodName.of("getData");
    //endregion
}
