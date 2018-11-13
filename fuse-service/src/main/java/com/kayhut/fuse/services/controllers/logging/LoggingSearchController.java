package com.kayhut.fuse.services.controllers.logging;

/*-
 * #%L
 * fuse-service
 * %%
 * Copyright (C) 2016 - 2018 kayhut
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.kayhut.fuse.dispatcher.logging.*;
import com.kayhut.fuse.dispatcher.logging.LogMessage.MDCWriter.Composite;
import com.kayhut.fuse.logging.RequestExternalMetadata;
import com.kayhut.fuse.logging.RequestId;
import com.kayhut.fuse.model.transport.ContentResponse;
import com.kayhut.fuse.model.transport.CreateQueryRequest;
import com.kayhut.fuse.services.controllers.SearchController;
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
public class LoggingSearchController extends LoggingControllerBase<SearchController> implements SearchController{
    public static final String controllerParameter = "LoggingSearchController.@controller";
    public static final String loggerParameter = "LoggingSearchController.@logger";

    //region Constructors
    @Inject
    public LoggingSearchController(
            @Named(controllerParameter) SearchController controller,
            @Named(loggerParameter) Logger logger,
            RequestIdSupplier requestIdSupplier,
            RequestExternalMetadataSupplier requestExternalMetadataSupplier,
            MetricRegistry metricRegistry) {
        super(controller, logger, requestIdSupplier, requestExternalMetadataSupplier, metricRegistry);
    }
    //endregion

    //region SearchController Implementation
    @Override
    public ContentResponse search(CreateQueryRequest request) {
        return new LoggingSyncMethodDecorator<ContentResponse<Object>>(
                this.logger,
                this.metricRegistry,
                search,
                this.primerMdcWriter(),
                Collections.singletonList(trace),
                Arrays.asList(info, trace))
                .decorate(() -> this.controller.search(request), this.resultHandler());
    }
    //endregion

    //region Fields
    private static MethodName.MDCWriter search = MethodName.of("search");
    //endregion
}
