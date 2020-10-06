package com.yangdb.fuse.services.controllers.languages.sparql;

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
import com.yangdb.fuse.dispatcher.logging.LoggingSyncMethodDecorator;
import com.yangdb.fuse.dispatcher.logging.MethodName;
import com.yangdb.fuse.model.ontology.Ontology;
import com.yangdb.fuse.model.transport.ContentResponse;
import com.yangdb.fuse.services.controllers.SchemaTranslatorController;
import com.yangdb.fuse.services.controllers.logging.LoggingControllerBase;
import com.yangdb.fuse.services.suppliers.RequestExternalMetadataSupplier;
import com.yangdb.fuse.services.suppliers.RequestIdSupplier;
import org.slf4j.Logger;

import java.util.Arrays;
import java.util.Collections;

import static com.yangdb.fuse.dispatcher.logging.LogMessage.Level.info;
import static com.yangdb.fuse.dispatcher.logging.LogMessage.Level.trace;

public class LoggingSparqlController extends LoggingControllerBase<SchemaTranslatorController> implements SchemaTranslatorController {
    public static final String controllerParameter = "LoggingSparqlController.@controller";
    public static final String loggerParameter = "LoggingSparqlController.@logger";

    //region Constructors
    @Inject
    public LoggingSparqlController(
            @Named(controllerParameter) SchemaTranslatorController controller,
            @Named(loggerParameter) Logger logger,
            RequestIdSupplier requestIdSupplier,
            RequestExternalMetadataSupplier requestExternalMetadataSupplier,
            MetricRegistry metricRegistry) {
        super(controller, logger, requestIdSupplier, requestExternalMetadataSupplier, metricRegistry);
    }
    //endregion

    //region CatalogController Implementation
    @Override
    public ContentResponse<Ontology> translate(String ontology, String sparqlSchema) {
        return new LoggingSyncMethodDecorator<ContentResponse<Ontology>>(
                this.logger,
                this.metricRegistry,
                translate,
                this.primerMdcWriter(),
                Collections.singletonList(trace),
                Arrays.asList(info, trace))
                .decorate(() -> this.controller.translate(ontology, sparqlSchema), this.resultHandler());
    }

    @Override
    public ContentResponse<String> transform(String ontologyId) {
        return new LoggingSyncMethodDecorator<ContentResponse<String>>(
                this.logger,
                this.metricRegistry,
                translate,
                this.primerMdcWriter(),
                Collections.singletonList(trace),
                Arrays.asList(info, trace))
                .decorate(() -> this.controller.transform(ontologyId), this.resultHandler());
    }

    //endregion

    //region Fields
    private static MethodName.MDCWriter translate = MethodName.of("translate");
    //endregion
}
