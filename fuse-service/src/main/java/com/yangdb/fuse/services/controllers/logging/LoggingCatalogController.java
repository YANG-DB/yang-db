package com.yangdb.fuse.services.controllers.logging;

/*-
 * #%L
 * fuse-service
 * %%
 * Copyright (C) 2016 - 2018 yangdb   ------ www.yangdb.org ------
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
import com.yangdb.fuse.services.suppliers.RequestExternalMetadataSupplier;
import com.yangdb.fuse.services.suppliers.RequestIdSupplier;
import com.yangdb.fuse.model.ontology.Ontology;
import com.yangdb.fuse.model.transport.ContentResponse;
import com.yangdb.fuse.services.controllers.CatalogController;
import org.slf4j.Logger;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static com.yangdb.fuse.dispatcher.logging.LogMessage.Level.info;
import static com.yangdb.fuse.dispatcher.logging.LogMessage.Level.trace;

/**
 * Created by roman.margolis on 14/12/2017.
 */
public class LoggingCatalogController extends LoggingControllerBase<CatalogController> implements CatalogController {
    public static final String controllerParameter = "LoggingCatalogController.@controller";
    public static final String loggerParameter = "LoggingCatalogController.@logger";

    //region Constructors
    @Inject
    public LoggingCatalogController(
            @Named(controllerParameter) CatalogController controller,
            @Named(loggerParameter) Logger logger,
            RequestIdSupplier requestIdSupplier,
            RequestExternalMetadataSupplier requestExternalMetadataSupplier,
            MetricRegistry metricRegistry) {
        super(controller, logger, requestIdSupplier, requestExternalMetadataSupplier, metricRegistry);
    }
    //endregion

    //region CatalogController Implementation
    @Override
    public ContentResponse<Ontology> getOntology(String id) {
        return new LoggingSyncMethodDecorator<ContentResponse<Ontology>>(
                this.logger,
                this.metricRegistry,
                getOntology,
                this.primerMdcWriter(),
                Collections.singletonList(trace),
                Arrays.asList(info, trace))
                .decorate(() -> this.controller.getOntology(id), this.resultHandler());
    }

    @Override
    public ContentResponse<Ontology> addOntology(Ontology ontology) {
        return new LoggingSyncMethodDecorator<ContentResponse<Ontology>>(
                this.logger,
                this.metricRegistry,
                addOntology,
                this.primerMdcWriter(),
                Collections.singletonList(trace),
                Arrays.asList(info, trace))
                .decorate(() -> this.controller.addOntology(ontology), this.resultHandler());

    }

    @Override
    public ContentResponse<List<Ontology>> getOntologies() {
        return new LoggingSyncMethodDecorator<ContentResponse<List<Ontology>>>(
                this.logger,
                this.metricRegistry,
                getOntologies,
                this.primerMdcWriter(),
                Collections.singletonList(trace),
                Arrays.asList(info, trace))
                .decorate(() -> this.controller.getOntologies(), this.resultHandler());
    }

    @Override
    public ContentResponse<String> getSchema(String id) {
        return new LoggingSyncMethodDecorator<ContentResponse<String>>(
                this.logger,
                this.metricRegistry,
                getSchema,
                this.primerMdcWriter(),
                Collections.singletonList(trace),
                Arrays.asList(info, trace))
                .decorate(() -> this.controller.getSchema(id), this.resultHandler());
    }

    @Override
    public ContentResponse<List<String>> getSchemas() {
        return new LoggingSyncMethodDecorator<ContentResponse<List<String>>>(
                this.logger,
                this.metricRegistry,
                getSchemas,
                this.primerMdcWriter(),
                Collections.singletonList(trace),
                Arrays.asList(info, trace))
                .decorate(() -> this.controller.getSchemas(), this.resultHandler());
    }
    //endregion

    //region Fields
    private static MethodName.MDCWriter getOntology = MethodName.of("getOntology");
    private static MethodName.MDCWriter getOntologies = MethodName.of("getOntologies");
    private static MethodName.MDCWriter addOntology = MethodName.of("addOntology");
    private static MethodName.MDCWriter getSchema = MethodName.of("getSchema");
    private static MethodName.MDCWriter getSchemas = MethodName.of("getSchemas");
    //endregion
}
