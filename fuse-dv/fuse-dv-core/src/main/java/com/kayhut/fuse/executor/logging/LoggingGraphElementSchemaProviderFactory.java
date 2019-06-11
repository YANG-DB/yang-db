package com.kayhut.fuse.executor.logging;

/*-
 * #%L
 * fuse-dv-core
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

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.kayhut.fuse.executor.ontology.GraphElementSchemaProviderFactory;
import com.kayhut.fuse.model.ontology.Ontology;
import com.kayhut.fuse.unipop.schemaProviders.GraphElementSchemaProvider;
import com.kayhut.fuse.unipop.schemaProviders.logging.LoggingGraphElementSchemaProvider;
import org.slf4j.Logger;

/**
 * Created by roman.margolis on 20/02/2018.
 */
public class LoggingGraphElementSchemaProviderFactory implements GraphElementSchemaProviderFactory {
    public static final String schemaProviderFactoryParameter = "LoggingGraphElementSchemaProviderFactory.@schemaProviderFactory";
    public static final String warnLoggerParameter = "LoggingGraphElementSchemaProviderFactory.@warnLogger";
    public static final String verboseLoggerParameter = "LoggingGraphElementSchemaProviderFactory.@verboseLogger";

    //region Constructors
    @Inject
    public LoggingGraphElementSchemaProviderFactory(
            @Named(schemaProviderFactoryParameter) GraphElementSchemaProviderFactory schemaProviderFactory,
            @Named(warnLoggerParameter) Logger warnLogger,
            @Named(verboseLoggerParameter) Logger verboseLogger) {
        this.schemaProviderFactory = schemaProviderFactory;
        this.warnLogger = warnLogger;
        this.verboseLogger = verboseLogger;
    }
    //endregion

    //region GraphElementSchemaProviderFactory Implementation
    @Override
    public GraphElementSchemaProvider get(Ontology ontology) {
        GraphElementSchemaProvider schemaProvider = this.schemaProviderFactory.get(ontology);
        return new LoggingGraphElementSchemaProvider(schemaProvider, this.warnLogger, this.verboseLogger);
    }
    //endregion

    //region Fields
    private GraphElementSchemaProviderFactory schemaProviderFactory;

    private Logger warnLogger;
    private Logger verboseLogger;
    //endregion
}
