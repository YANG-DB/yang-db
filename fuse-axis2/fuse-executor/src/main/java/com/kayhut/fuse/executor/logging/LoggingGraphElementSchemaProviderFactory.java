package com.kayhut.fuse.executor.logging;

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
