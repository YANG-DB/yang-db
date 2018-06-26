package com.kayhut.fuse.services.controllers.logging;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.kayhut.fuse.dispatcher.logging.*;
import com.kayhut.fuse.dispatcher.logging.LogMessage.MDCWriter.Composite;
import com.kayhut.fuse.logging.RequestId;
import com.kayhut.fuse.model.transport.ContentResponse;
import com.kayhut.fuse.services.controllers.DataLoaderController;
import com.kayhut.fuse.services.suppliers.RequestIdSupplier;
import org.slf4j.Logger;

import java.util.concurrent.TimeUnit;

import static com.codahale.metrics.MetricRegistry.name;
import static com.kayhut.fuse.dispatcher.logging.LogMessage.Level.*;
import static com.kayhut.fuse.dispatcher.logging.LogType.*;

/**
 * Created by roman.margolis on 14/12/2017.
 */
public class LoggingDataLoaderController implements DataLoaderController {
    public static final String controllerParameter = "LoggingDataLoaderController.@controller";
    public static final String loggerParameter = "LoggingDataLoaderController.@logger";

    //region Constructors
    @Inject
    public LoggingDataLoaderController(
            @Named(controllerParameter) DataLoaderController controller,
            @Named(loggerParameter) Logger logger,
            RequestIdSupplier requestIdSupplier,
            MetricRegistry metricRegistry) {
        this.controller = controller;
        this.logger = logger;
        this.requestIdSupplier = requestIdSupplier;
        this.metricRegistry = metricRegistry;
    }
    //endregion

    //region CatalogController Implementation
    @Override
    public ContentResponse<String> load(String ontology ) {
        Timer.Context timerContext = this.metricRegistry.timer(name(this.logger.getName(), load.toString())).time();

        Composite.of(Elapsed.now(), ElapsedFrom.now(), RequestId.of(this.requestIdSupplier.get())).write();

        ContentResponse<String> response = null;

        try {
            new LogMessage.Impl(this.logger, trace, "start load", sequence, LogType.of(start), load).log();
            response = this.controller.load(ontology);
            new LogMessage.Impl(this.logger, info, "finish load", sequence, LogType.of(success), load, ElapsedFrom.now()).log();
            new LogMessage.Impl(this.logger, trace, "finish load", sequence, LogType.of(success), load, ElapsedFrom.now()).log();
            this.metricRegistry.meter(name(this.logger.getName(), load.toString(), "success")).mark();
        } catch (Exception ex) {
            new LogMessage.Impl(this.logger, error, "failed load", sequence, LogType.of(failure), load, ElapsedFrom.now())
                    .with(ex).log();
            this.metricRegistry.meter(name(this.logger.getName(), load.toString(), "failure")).mark();
            response = ContentResponse.internalError(ex);
        }

        return ContentResponse.Builder.builder(response)
                .requestId(this.requestIdSupplier.get())
                .elapsed(TimeUnit.MILLISECONDS.convert(timerContext.stop(), TimeUnit.NANOSECONDS))
                .compose();
    }
    //region CatalogController Implementation
    @Override
    public ContentResponse<String> init(String ontology ) {
        Timer.Context timerContext = this.metricRegistry.timer(name(this.logger.getName(), init.toString())).time();

        Composite.of(Elapsed.now(), ElapsedFrom.now(), RequestId.of(this.requestIdSupplier.get())).write();

        ContentResponse<String> response = null;

        try {
            new LogMessage.Impl(this.logger, trace, "start init", sequence, LogType.of(start), init).log();
            response = this.controller.init(ontology);
            new LogMessage.Impl(this.logger, info, "finish init", sequence, LogType.of(success), init, ElapsedFrom.now()).log();
            new LogMessage.Impl(this.logger, trace, "finish init", sequence, LogType.of(success), init, ElapsedFrom.now()).log();
            this.metricRegistry.meter(name(this.logger.getName(), init.toString(), "success")).mark();
        } catch (Exception ex) {
            new LogMessage.Impl(this.logger, error, "failed init", sequence, LogType.of(failure), init, ElapsedFrom.now())
                    .with(ex).log();
            this.metricRegistry.meter(name(this.logger.getName(), init.toString(), "failure")).mark();
            response = ContentResponse.internalError(ex);
        }

        return ContentResponse.Builder.builder(response)
                .requestId(this.requestIdSupplier.get())
                .elapsed(TimeUnit.MILLISECONDS.convert(timerContext.stop(), TimeUnit.NANOSECONDS))
                .compose();
    }
    //region CatalogController Implementation
    @Override
    public ContentResponse<String> drop(String ontology ) {
        Timer.Context timerContext = this.metricRegistry.timer(name(this.logger.getName(), drop.toString())).time();

        Composite.of(Elapsed.now(), ElapsedFrom.now(), RequestId.of(this.requestIdSupplier.get())).write();

        ContentResponse<String> response = null;

        try {
            new LogMessage.Impl(this.logger, trace, "start drop", sequence, LogType.of(start), drop).log();
            response = this.controller.drop(ontology);
            new LogMessage.Impl(this.logger, info, "finish drop", sequence, LogType.of(success), drop, ElapsedFrom.now()).log();
            new LogMessage.Impl(this.logger, trace, "finish drop", sequence, LogType.of(success), drop, ElapsedFrom.now()).log();
            this.metricRegistry.meter(name(this.logger.getName(), drop.toString(), "success")).mark();
        } catch (Exception ex) {
            new LogMessage.Impl(this.logger, error, "failed drop", sequence, LogType.of(failure), drop, ElapsedFrom.now())
                    .with(ex).log();
            this.metricRegistry.meter(name(this.logger.getName(), drop.toString(), "failure")).mark();
            response = ContentResponse.internalError(ex);
        }

        return ContentResponse.Builder.builder(response)
                .requestId(this.requestIdSupplier.get())
                .elapsed(TimeUnit.MILLISECONDS.convert(timerContext.stop(), TimeUnit.NANOSECONDS))
                .compose();
    }
    //endregion

    //region Fields
    private Logger logger;
    private RequestIdSupplier requestIdSupplier;
    private MetricRegistry metricRegistry;
    private DataLoaderController controller;

    private static MethodName.MDCWriter load = MethodName.of("load");
    private static MethodName.MDCWriter init = MethodName.of("init");
    private static MethodName.MDCWriter drop = MethodName.of("drop");

    private static LogMessage.MDCWriter sequence = Sequence.incr();
    //endregion
}
