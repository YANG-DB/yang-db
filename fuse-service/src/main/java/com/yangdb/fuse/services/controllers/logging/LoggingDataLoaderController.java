package com.yangdb.fuse.services.controllers.logging;

/*-
 * #%L
 * fuse-service
 * %%
 * Copyright (C) 2016 - 2019 The YangDb Graph Database Project
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
import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.yangdb.fuse.dispatcher.logging.*;
import com.yangdb.fuse.executor.ontology.schema.load.GraphDataLoader;
import com.yangdb.fuse.executor.ontology.schema.load.LoadResponse;
import com.yangdb.fuse.model.logical.LogicalGraphModel;
import com.yangdb.fuse.model.resourceInfo.FuseError;
import com.yangdb.fuse.model.transport.ContentResponse;
import com.yangdb.fuse.services.controllers.DataLoaderController;
import com.yangdb.fuse.services.suppliers.RequestExternalMetadataSupplier;
import com.yangdb.fuse.services.suppliers.RequestIdSupplier;
import org.slf4j.Logger;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;

import static com.codahale.metrics.MetricRegistry.name;
import static com.yangdb.fuse.dispatcher.logging.LogMessage.Level.*;

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
    public ContentResponse<LoadResponse<String, FuseError>> loadGraph(String ontology, LogicalGraphModel data, GraphDataLoader.Directive directive) {
        return new LoggingSyncMethodDecorator<ContentResponse<LoadResponse<String, FuseError>>>(
                this.logger,
                this.metricRegistry,
                load,
                this.primerMdcWriter(),
                Collections.singletonList(trace),
                Arrays.asList(info, trace))
                .decorate(() -> this.controller.loadGraph(ontology, data,directive ), this.resultHandler());
    }

    @Override
    public ContentResponse<LoadResponse<String, FuseError>> loadCsv(String ontology, String type, String label, String data, GraphDataLoader.Directive directive) {
        return new LoggingSyncMethodDecorator<ContentResponse<LoadResponse<String, FuseError>>>(
                this.logger,
                this.metricRegistry,
                load,
                this.primerMdcWriter(),
                Collections.singletonList(trace),
                Arrays.asList(info, trace))
                .decorate(() -> this.controller.loadCsv(ontology,type, label, data, directive), this.resultHandler());
    }

    @Override
    public ContentResponse<LoadResponse<String, FuseError>> loadGraph(String ontology, File data, GraphDataLoader.Directive directive) {
        return new LoggingSyncMethodDecorator<ContentResponse<LoadResponse<String, FuseError>>>(
                this.logger,
                this.metricRegistry,
                load,
                this.primerMdcWriter(),
                Collections.singletonList(trace),
                Arrays.asList(info, trace))
                .decorate(() -> this.controller.loadGraph(ontology, data,directive ), this.resultHandler());
    }

    @Override
    public ContentResponse<LoadResponse<String, FuseError>> loadCsv(String ontology, String type, String label, File data, GraphDataLoader.Directive directive) {
        return new LoggingSyncMethodDecorator<ContentResponse<LoadResponse<String, FuseError>>>(
                this.logger,
                this.metricRegistry,
                load,
                this.primerMdcWriter(),
                Collections.singletonList(trace),
                Arrays.asList(info, trace))
                .decorate(() -> this.controller.loadCsv(ontology,type,label , data, directive), this.resultHandler());
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
    public ContentResponse<String> createMapping(String ontology,String indexProvider ) {
        return new LoggingSyncMethodDecorator<ContentResponse<String>>(
                this.logger,
                this.metricRegistry,
                init,
                this.primerMdcWriter(),
                Collections.singletonList(trace),
                Arrays.asList(info, trace))
                .decorate(() -> this.controller.createMapping(ontology,indexProvider), this.resultHandler());
    }
    @Override
    public ContentResponse<String> createIndices(String ontology,String indexProvider ) {
        return new LoggingSyncMethodDecorator<ContentResponse<String>>(
                this.logger,
                this.metricRegistry,
                init,
                this.primerMdcWriter(),
                Collections.singletonList(trace),
                Arrays.asList(info, trace))
                .decorate(() -> this.controller.createIndices(ontology,indexProvider), this.resultHandler());
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
