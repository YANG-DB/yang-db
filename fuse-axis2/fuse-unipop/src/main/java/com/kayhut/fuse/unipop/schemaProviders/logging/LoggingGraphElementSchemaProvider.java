package com.kayhut.fuse.unipop.schemaProviders.logging;

import com.google.inject.name.Named;
import com.kayhut.fuse.dispatcher.logging.*;
import com.kayhut.fuse.unipop.schemaProviders.GraphEdgeSchema;
import com.kayhut.fuse.unipop.schemaProviders.GraphElementPropertySchema;
import com.kayhut.fuse.unipop.schemaProviders.GraphElementSchemaProvider;
import com.kayhut.fuse.unipop.schemaProviders.GraphVertexSchema;
import javaslang.collection.Stream;
import org.apache.tinkerpop.gremlin.structure.Direction;
import org.slf4j.Logger;

import java.util.Optional;

import static com.kayhut.fuse.dispatcher.logging.LogMessage.Level.*;
import static com.kayhut.fuse.dispatcher.logging.LogType.*;

/**
 * Created by roman.margolis on 20/02/2018.
 */
public class LoggingGraphElementSchemaProvider implements GraphElementSchemaProvider {
    public static final String schemaProviderParameter = "LoggingGraphElementSchemaProvider.@schemaProviderParameter";
    public static final String warnLoggerParameter = "LoggingGraphElementSchemaProvider.@warnLogger";
    public static final String verboseLoggerParameter = "LoggingGraphElementSchemaProvider.@verboseLogger";

    //region Constructors
    public LoggingGraphElementSchemaProvider(
            @Named(schemaProviderParameter) GraphElementSchemaProvider schemaProvider,
            @Named(warnLoggerParameter) Logger warnLogger,
            @Named(verboseLoggerParameter) Logger verboseLogger) {
        this.schemaProvider = schemaProvider;
        this.warnLogger = warnLogger;
        this.verboseLogger = verboseLogger;
    }
    //endregion

    //region GraphElementSchemaProvider Implementation
    @Override
    public Iterable<GraphVertexSchema> getVertexSchemas(String label) {
        boolean thrownException = false;

        try {
            new LogMessage.Impl(this.verboseLogger, trace, "start getVertexSchemas label: {}", LogType.of(start), getVertexSchemas, ElapsedFrom.now())
                .with(label).log();
            Iterable<GraphVertexSchema> vertexSchemas = this.schemaProvider.getVertexSchemas(label);
            if (Stream.ofAll(vertexSchemas).isEmpty()) {
                new LogMessage.Impl(this.warnLogger, warn, "no vertex schema found for label: {}",
                        LogType.of(log), getVertexSchemas, ElapsedFrom.now()).with(label).log();
            }
            return vertexSchemas;
        } catch (Exception ex) {
            thrownException = true;
            new LogMessage.Impl(this.verboseLogger, error, "failed getVertexSchemas label: {}", LogType.of(failure), getVertexSchemas, ElapsedFrom.now())
                    .with(label, ex).log();
            throw ex;
        } finally {
            if (!thrownException) {
                new LogMessage.Impl(this.verboseLogger, trace, "finish getVertexSchemas label: {}", LogType.of(success), getVertexSchemas, ElapsedFrom.now())
                        .with(label).log();
            }
        }
    }

    @Override
    public Iterable<GraphEdgeSchema> getEdgeSchemas(String label) {
        boolean thrownException = false;

        try {
            new LogMessage.Impl(this.verboseLogger, trace, "start getEdgeSchemas label: {}", LogType.of(start), getEdgeSchemas, ElapsedFrom.now())
                    .with(label).log();
            Iterable<GraphEdgeSchema> edgeSchemas = this.schemaProvider.getEdgeSchemas(label);
            if (Stream.ofAll(edgeSchemas).isEmpty()) {
                new LogMessage.Impl(this.warnLogger, warn, "no edge schema found for label: {}",
                        LogType.of(log), getEdgeSchemas, ElapsedFrom.now()).with(label).log();
            }
            return edgeSchemas;
        } catch (Exception ex) {
            thrownException = true;
            new LogMessage.Impl(this.verboseLogger, error, "failed getEdgeSchemas label: {}", LogType.of(failure), getEdgeSchemas, ElapsedFrom.now())
                    .with(label, ex).log();
            throw ex;
        } finally {
            if (!thrownException) {
                new LogMessage.Impl(this.verboseLogger, trace, "finish getEdgeSchemas label: {}", LogType.of(success), getEdgeSchemas, ElapsedFrom.now())
                        .with(label).log();
            }
        }
    }

    @Override
    public Iterable<GraphEdgeSchema> getEdgeSchemas(String vertexLabelA, String label) {
        boolean thrownException = false;

        try {
            new LogMessage.Impl(this.verboseLogger, trace, "start getEdgeSchemas vertexLabelA: {}, label: {}", LogType.of(start), getEdgeSchemas, ElapsedFrom.now())
                    .with(vertexLabelA, label).log();
            Iterable<GraphEdgeSchema> edgeSchemas = this.schemaProvider.getEdgeSchemas(vertexLabelA, label);
            if (Stream.ofAll(edgeSchemas).isEmpty()) {
                new LogMessage.Impl(this.warnLogger, warn, "no edge schema found for vertexLabelA: {}, label: {}",
                        LogType.of(log), getEdgeSchemas, ElapsedFrom.now()).with(vertexLabelA, label).log();
            }
            return edgeSchemas;
        } catch (Exception ex) {
            thrownException = true;
            new LogMessage.Impl(this.verboseLogger, error, "failed getEdgeSchemas vertexLabelA: {}, label: {}", LogType.of(failure), getEdgeSchemas, ElapsedFrom.now())
                    .with(vertexLabelA, label, ex).log();
            throw ex;
        } finally {
            if (!thrownException) {
                new LogMessage.Impl(this.verboseLogger, trace, "finish getEdgeSchemas vertexLabelA: {}, label: {}", LogType.of(success), getEdgeSchemas, ElapsedFrom.now())
                        .with(vertexLabelA, label).log();
            }
        }
    }

    @Override
    public Iterable<GraphEdgeSchema> getEdgeSchemas(String vertexLabelA, Direction direction, String label) {
        boolean thrownException = false;

        try {
            new LogMessage.Impl(this.verboseLogger, trace, "start getEdgeSchemas vertexLabelA: {}, direction: {}, label: {}", LogType.of(start), getEdgeSchemas, ElapsedFrom.now())
                    .with(vertexLabelA, direction.toString(), label).log();
            Iterable<GraphEdgeSchema> edgeSchemas = this.schemaProvider.getEdgeSchemas(vertexLabelA, direction, label);
            if (Stream.ofAll(edgeSchemas).isEmpty()) {
                new LogMessage.Impl(this.warnLogger, warn, "no edge schema found for vertexLabelA: {}, direction: {}, label: {}",
                        LogType.of(log), getEdgeSchemas, ElapsedFrom.now()).with(vertexLabelA, direction.toString(), label).log();
            }
            return edgeSchemas;
        } catch (Exception ex) {
            thrownException = true;
            new LogMessage.Impl(this.verboseLogger, error, "failed getEdgeSchemas vertexLabelA: {}, direction: {}, label: {}", LogType.of(failure), getEdgeSchemas, ElapsedFrom.now())
                    .with(vertexLabelA, direction.toString(), label, ex).log();
            throw ex;
        } finally {
            if (!thrownException) {
                new LogMessage.Impl(this.verboseLogger, trace, "finish getEdgeSchemas vertexLabelA: {}, direction: {}, label: {}", LogType.of(success), getEdgeSchemas, ElapsedFrom.now())
                        .with(vertexLabelA, direction.toString(), label).log();
            }
        }
    }

    @Override
    public Iterable<GraphEdgeSchema> getEdgeSchemas(String vertexLabelA, Direction direction, String label, String vertexLabelB) {
        boolean thrownException = false;

        try {
            new LogMessage.Impl(this.verboseLogger, trace, "start getEdgeSchemas vertexLabelA: {}, direction: {}, label: {}, vertexLabelB: {}", LogType.of(start), getEdgeSchemas, ElapsedFrom.now())
                    .with(vertexLabelA, direction.toString(), label, vertexLabelB).log();
            Iterable<GraphEdgeSchema> edgeSchemas = this.schemaProvider.getEdgeSchemas(vertexLabelA, direction, label, vertexLabelB);
            if (Stream.ofAll(edgeSchemas).isEmpty()) {
                new LogMessage.Impl(this.warnLogger, warn, "no edge schema found for vertexLabelA: {}, direction: {}, label: {}, vertexLabelB: {}",
                        LogType.of(log), getEdgeSchemas, ElapsedFrom.now()).with(vertexLabelA, direction.toString(), label, vertexLabelB).log();
            }
            return edgeSchemas;
        } catch (Exception ex) {
            thrownException = true;
            new LogMessage.Impl(this.verboseLogger, error, "failed getEdgeSchemas vertexLabelA: {}, direction: {}, label: {}, vertexLabelB: {}", LogType.of(failure), getEdgeSchemas, ElapsedFrom.now())
                    .with(vertexLabelA, direction.toString(), label, vertexLabelB, ex).log();
            throw ex;
        } finally {
            if (!thrownException) {
                new LogMessage.Impl(this.verboseLogger, trace, "finish getEdgeSchemas vertexLabelA: {}, direction: {}, label: {}, vertexLabelB: {}", LogType.of(success), getEdgeSchemas, ElapsedFrom.now())
                        .with(vertexLabelA, direction.toString(), label, vertexLabelB).log();
            }
        }
    }

    @Override
    public Optional<GraphElementPropertySchema> getPropertySchema(String name) {
        boolean thrownException = false;

        try {
            new LogMessage.Impl(this.verboseLogger, trace, "start getPropertySchema name: {}", LogType.of(start), getPropertySchema, ElapsedFrom.now())
                    .with(name).log();
            Optional<GraphElementPropertySchema> propertySchema = this.schemaProvider.getPropertySchema(name);
            if (!propertySchema.isPresent()) {
                new LogMessage.Impl(this.warnLogger, warn, "no property schema found for name: {}",
                        LogType.of(log), getPropertySchema, ElapsedFrom.now()).with(name).log();
            }
            return propertySchema;
        } catch (Exception ex) {
            thrownException = true;
            new LogMessage.Impl(this.verboseLogger, error, "failed getPropertySchema name: {}", LogType.of(failure), getPropertySchema, ElapsedFrom.now())
                    .with(name, ex).log();
            throw ex;
        } finally {
            if (!thrownException) {
                new LogMessage.Impl(this.verboseLogger, trace, "finish getPropertySchema name: {}", LogType.of(success), getPropertySchema, ElapsedFrom.now())
                        .with(name).log();
            }
        }
    }

    @Override
    public Iterable<String> getVertexLabels() {
        boolean thrownException = false;

        try {
            new LogMessage.Impl(this.verboseLogger, trace, "start getVertexLabels", LogType.of(start), getVertexLabels, ElapsedFrom.now()).log();
            Iterable<String> vertexLabels = this.schemaProvider.getVertexLabels();
            if (Stream.ofAll(vertexLabels).isEmpty()) {
                new LogMessage.Impl(this.warnLogger, warn, "no vertex labels found",
                        LogType.of(log), getVertexLabels, ElapsedFrom.now()).log();
            }
            return vertexLabels;
        } catch (Exception ex) {
            thrownException = true;
            new LogMessage.Impl(this.verboseLogger, error, "failed getVertexLabels", LogType.of(failure), getVertexLabels, ElapsedFrom.now())
                    .with(ex).log();
            throw ex;
        } finally {
            if (!thrownException) {
                new LogMessage.Impl(this.verboseLogger, trace, "finish getVertexLabels", LogType.of(success), getVertexLabels, ElapsedFrom.now()).log();
            }
        }
    }

    @Override
    public Iterable<String> getEdgeLabels() {
        boolean thrownException = false;

        try {
            new LogMessage.Impl(this.verboseLogger, trace, "start getEdgeLabels", LogType.of(start), getEdgeLabels, ElapsedFrom.now()).log();
            Iterable<String> vertexLabels = this.schemaProvider.getVertexLabels();
            if (Stream.ofAll(vertexLabels).isEmpty()) {
                new LogMessage.Impl(this.warnLogger, warn, "no edge labels found",
                        LogType.of(log), getVertexLabels, ElapsedFrom.now()).log();
            }
            return vertexLabels;
        } catch (Exception ex) {
            thrownException = true;
            new LogMessage.Impl(this.verboseLogger, error, "failed getEdgeLabels", LogType.of(failure), getEdgeLabels, ElapsedFrom.now())
                    .with(ex).log();
            throw ex;
        } finally {
            if (!thrownException) {
                new LogMessage.Impl(this.verboseLogger, trace, "finish getEdgeLabels", LogType.of(success), getEdgeLabels, ElapsedFrom.now()).log();
            }
        }
    }
    //endregion

    //region Fields
    private GraphElementSchemaProvider schemaProvider;
    private Logger warnLogger;
    private Logger verboseLogger;

    private static MethodName.MDCWriter getVertexSchemas = MethodName.of("getVertexSchemas");
    private static MethodName.MDCWriter getEdgeSchemas = MethodName.of("getEdgeSchemas");
    private static MethodName.MDCWriter getPropertySchema = MethodName.of("getPropertySchema");
    private static MethodName.MDCWriter getVertexLabels = MethodName.of("getVertexLabels");
    private static MethodName.MDCWriter getEdgeLabels = MethodName.of("getEdgeLabels");
    //endregion
}
