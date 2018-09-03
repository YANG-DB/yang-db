package com.kayhut.fuse.services.controllers.logging;

import com.codahale.metrics.MetricRegistry;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.kayhut.fuse.dispatcher.logging.*;
import com.kayhut.fuse.services.suppliers.RequestExternalMetadataSupplier;
import com.kayhut.fuse.services.suppliers.RequestIdSupplier;
import com.kayhut.fuse.model.resourceInfo.FuseResourceInfo;
import com.kayhut.fuse.model.transport.ContentResponse;
import com.kayhut.fuse.services.controllers.ApiDescriptionController;
import org.slf4j.Logger;

import java.util.Arrays;
import java.util.Collections;

import static com.kayhut.fuse.dispatcher.logging.LogMessage.Level.*;

/**
 * Created by roman.margolis on 14/12/2017.
 */
public class LoggingApiDescriptionController extends LoggingControllerBase<ApiDescriptionController> implements ApiDescriptionController {
    public static final String controllerParameter = "LoggingApiDescriptionController.@controller";
    public static final String loggerParameter = "LoggingApiDescriptionController.@logger";

    //region Constructors
    @Inject
    public LoggingApiDescriptionController(
            @Named(controllerParameter) ApiDescriptionController controller,
            @Named(loggerParameter) Logger logger,
            RequestIdSupplier requestIdSupplier,
            RequestExternalMetadataSupplier requestExternalMetadataSupplier,
            MetricRegistry metricRegistry) {
        super(controller, logger, requestIdSupplier, requestExternalMetadataSupplier, metricRegistry);
    }
    //endregion

    //region ApiDescriptionController Implementation
    @Override
    public ContentResponse<FuseResourceInfo> getInfo() {
        return new LoggingSyncMethodDecorator<ContentResponse<FuseResourceInfo>>(
                this.logger,
                this.metricRegistry,
                getInfo,
                this.primerMdcWriter(),
                Collections.singletonList(trace),
                Arrays.asList(info, trace))
                .decorate(() -> this.controller.getInfo(), this.resultHandler());
    }
    //endregion

    //region Fields
    private static MethodName.MDCWriter getInfo = MethodName.of("getInfo");
    //endregion
}
