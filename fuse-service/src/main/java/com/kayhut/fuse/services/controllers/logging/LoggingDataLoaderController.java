package com.kayhut.fuse.services.controllers.logging;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.kayhut.fuse.dispatcher.logging.*;
import com.kayhut.fuse.dispatcher.logging.LogMessage.MDCWriter.Composite;
import com.kayhut.fuse.logging.RequestId;
import com.kayhut.fuse.model.resourceInfo.FuseResourceInfo;
import com.kayhut.fuse.model.transport.ContentResponse;
import com.kayhut.fuse.services.controllers.DataLoaderController;
import com.kayhut.fuse.services.suppliers.RequestExternalMetadataSupplier;
import com.kayhut.fuse.services.suppliers.RequestIdSupplier;
import org.slf4j.Logger;

import java.util.Arrays;
import java.util.Collections;
import java.util.concurrent.TimeUnit;

import static com.codahale.metrics.MetricRegistry.name;
import static com.kayhut.fuse.dispatcher.logging.LogMessage.Level.*;
import static com.kayhut.fuse.dispatcher.logging.LogType.*;

/**
 * Created by roman.margolis on 14/12/2017.
 */
public class LoggingDataLoaderController extends LoggingControllerBase<DataLoaderController> implements DataLoaderController {
    public static final String controllerParameter = "LoggingDataLoaderController.@controller";
    public static final String loggerParameter = "LoggingDataLoaderController.@logger";

    //region Constructors
    @Inject
    public LoggingDataLoaderController(
            @Named(controllerParameter) DataLoaderController controller,
            @Named(loggerParameter) Logger logger,
            RequestIdSupplier requestIdSupplier,
            RequestExternalMetadataSupplier requestExternalMetadataSupplier,
            MetricRegistry metricRegistry) {
        super(controller, logger, requestIdSupplier, requestExternalMetadataSupplier, metricRegistry);
    }
    //endregion

    //region CatalogController Implementation
    @Override
    public ContentResponse<String> load(String ontology ) {
        return new LoggingSyncMethodDecorator<ContentResponse<String>>(
                this.logger,
                this.metricRegistry,
                load,
                this.primerMdcWriter(),
                Collections.singletonList(trace),
                Arrays.asList(info, trace))
                .decorate(() -> this.controller.load(ontology), this.resultHandler());
    }

    @Override
    public ContentResponse<String> init(String ontology ) {
        return new LoggingSyncMethodDecorator<ContentResponse<String>>(
                this.logger,
                this.metricRegistry,
                init,
                this.primerMdcWriter(),
                Collections.singletonList(trace),
                Arrays.asList(info, trace))
                .decorate(() -> this.controller.init(ontology), this.resultHandler());
    }

    @Override
    public ContentResponse<String> drop(String ontology ) {
        return new LoggingSyncMethodDecorator<ContentResponse<String>>(
                this.logger,
                this.metricRegistry,
                drop,
                this.primerMdcWriter(),
                Collections.singletonList(trace),
                Arrays.asList(info, trace))
                .decorate(() -> this.controller.drop(ontology), this.resultHandler());
    }
    //endregion

    //region Fields
    private static MethodName.MDCWriter load = MethodName.of("load");
    private static MethodName.MDCWriter init = MethodName.of("init");
    private static MethodName.MDCWriter drop = MethodName.of("drop");
    //endregion
}
