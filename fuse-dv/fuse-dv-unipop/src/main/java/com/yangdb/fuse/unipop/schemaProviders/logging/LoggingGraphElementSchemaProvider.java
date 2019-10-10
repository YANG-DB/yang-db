package com.yangdb.fuse.unipop.schemaProviders.logging;

/*-
 *
 * fuse-dv-unipop
 * %%
 * Copyright (C) 2016 - 2019 yangdb   ------ www.yangdb.org ------
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
 *
 */

import com.google.inject.name.Named;
import com.yangdb.fuse.dispatcher.logging.*;
import com.yangdb.fuse.unipop.schemaProviders.GraphEdgeSchema;
import com.yangdb.fuse.unipop.schemaProviders.GraphElementPropertySchema;
import com.yangdb.fuse.unipop.schemaProviders.GraphElementSchemaProvider;
import com.yangdb.fuse.unipop.schemaProviders.GraphVertexSchema;
import javaslang.collection.Stream;
import org.apache.tinkerpop.gremlin.structure.Direction;
import org.slf4j.Logger;

import java.util.Optional;

import static com.yangdb.fuse.dispatcher.logging.LogMessage.Level.*;
import static com.yangdb.fuse.dispatcher.logging.LogType.*;

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
            new LogMessage.Impl(this.verboseLogger, trace, "start getVertexSchemas label: {}", sequence, LogType.of(start), getVertexSchemas, ElapsedFrom.now())
                .with(label).log();
            Iterable<GraphVertexSchema> vertexSchemas = this.schemaProvider.getVertexSchemas(label);
            if (Stream.ofAll(vertexSchemas).isEmpty()) {
                new LogMessage.Impl(this.warnLogger, warn, "no vertex schema found for label: {}",
                        sequence, LogType.of(log), getVertexSchemas, ElapsedFrom.now()).with(label).log();
            }
            return vertexSchemas;
        } catch (Exception ex) {
            thrownException = true;
            new LogMessage.Impl(this.verboseLogger, error, "failed getVertexSchemas label: {}", sequence, LogType.of(failure), getVertexSchemas, ElapsedFrom.now())
                    .with(label, ex).log();
            throw ex;
        } finally {
            if (!thrownException) {
                new LogMessage.Impl(this.verboseLogger, trace, "finish getVertexSchemas label: {}", sequence, LogType.of(success), getVertexSchemas, ElapsedFrom.now())
                        .with(label).log();
            }
        }
    }

    @Override
    public Iterable<GraphEdgeSchema> getEdgeSchemas(String label) {
        boolean thrownException = false;

        try {
            new LogMessage.Impl(this.verboseLogger, trace, "start getEdgeSchemas label: {}", sequence, LogType.of(start), getEdgeSchemas, ElapsedFrom.now())
                    .with(label).log();
            Iterable<GraphEdgeSchema> edgeSchemas = this.schemaProvider.getEdgeSchemas(label);
            if (Stream.ofAll(edgeSchemas).isEmpty()) {
                new LogMessage.Impl(this.warnLogger, warn, "no edge schema found for label: {}",
                        sequence, LogType.of(log), getEdgeSchemas, ElapsedFrom.now()).with(label).log();
            }
            return edgeSchemas;
        } catch (Exception ex) {
            thrownException = true;
            new LogMessage.Impl(this.verboseLogger, error, "failed getEdgeSchemas label: {}", sequence, LogType.of(failure), getEdgeSchemas, ElapsedFrom.now())
                    .with(label, ex).log();
            throw ex;
        } finally {
            if (!thrownException) {
                new LogMessage.Impl(this.verboseLogger, trace, "finish getEdgeSchemas label: {}", sequence, LogType.of(success), getEdgeSchemas, ElapsedFrom.now())
                        .with(label).log();
            }
        }
    }

    @Override
    public Iterable<GraphEdgeSchema> getEdgeSchemas(String vertexLabelA, String label) {
        boolean thrownException = false;

        try {
            new LogMessage.Impl(this.verboseLogger, trace, "start getEdgeSchemas vertexLabelA: {}, label: {}", sequence, LogType.of(start), getEdgeSchemas, ElapsedFrom.now())
                    .with(vertexLabelA, label).log();
            Iterable<GraphEdgeSchema> edgeSchemas = this.schemaProvider.getEdgeSchemas(vertexLabelA, label);
            if (Stream.ofAll(edgeSchemas).isEmpty()) {
                new LogMessage.Impl(this.warnLogger, warn, "no edge schema found for vertexLabelA: {}, label: {}",
                        sequence, LogType.of(log), getEdgeSchemas, ElapsedFrom.now()).with(vertexLabelA, label).log();
            }
            return edgeSchemas;
        } catch (Exception ex) {
            thrownException = true;
            new LogMessage.Impl(this.verboseLogger, error, "failed getEdgeSchemas vertexLabelA: {}, label: {}", sequence, LogType.of(failure), getEdgeSchemas, ElapsedFrom.now())
                    .with(vertexLabelA, label, ex).log();
            throw ex;
        } finally {
            if (!thrownException) {
                new LogMessage.Impl(this.verboseLogger, trace, "finish getEdgeSchemas vertexLabelA: {}, label: {}", sequence, LogType.of(success), getEdgeSchemas, ElapsedFrom.now())
                        .with(vertexLabelA, label).log();
            }
        }
    }

    @Override
    public Iterable<GraphEdgeSchema> getEdgeSchemas(String vertexLabelA, Direction direction, String label) {
        boolean thrownException = false;

        try {
            new LogMessage.Impl(this.verboseLogger, trace, "start getEdgeSchemas vertexLabelA: {}, direction: {}, label: {}", sequence, LogType.of(start), getEdgeSchemas, ElapsedFrom.now())
                    .with(vertexLabelA, direction.toString(), label).log();
            Iterable<GraphEdgeSchema> edgeSchemas = this.schemaProvider.getEdgeSchemas(vertexLabelA, direction, label);
            if (Stream.ofAll(edgeSchemas).isEmpty()) {
                new LogMessage.Impl(this.warnLogger, warn, "no edge schema found for vertexLabelA: {}, direction: {}, label: {}",
                        sequence, LogType.of(log), getEdgeSchemas, ElapsedFrom.now()).with(vertexLabelA, direction.toString(), label).log();
            }
            return edgeSchemas;
        } catch (Exception ex) {
            thrownException = true;
            new LogMessage.Impl(this.verboseLogger, error, "failed getEdgeSchemas vertexLabelA: {}, direction: {}, label: {}", sequence, LogType.of(failure), getEdgeSchemas, ElapsedFrom.now())
                    .with(vertexLabelA, direction.toString(), label, ex).log();
            throw ex;
        } finally {
            if (!thrownException) {
                new LogMessage.Impl(this.verboseLogger, trace, "finish getEdgeSchemas vertexLabelA: {}, direction: {}, label: {}", sequence, LogType.of(success), getEdgeSchemas, ElapsedFrom.now())
                        .with(vertexLabelA, direction.toString(), label).log();
            }
        }
    }

    @Override
    public Iterable<GraphEdgeSchema> getEdgeSchemas(String vertexLabelA, Direction direction, String label, String vertexLabelB) {
        boolean thrownException = false;

        try {
            new LogMessage.Impl(this.verboseLogger, trace, "start getEdgeSchemas vertexLabelA: {}, direction: {}, label: {}, vertexLabelB: {}", sequence, LogType.of(start), getEdgeSchemas, ElapsedFrom.now())
                    .with(vertexLabelA, direction.toString(), label, vertexLabelB).log();
            Iterable<GraphEdgeSchema> edgeSchemas = this.schemaProvider.getEdgeSchemas(vertexLabelA, direction, label, vertexLabelB);
            if (Stream.ofAll(edgeSchemas).isEmpty()) {
                new LogMessage.Impl(this.warnLogger, warn, "no edge schema found for vertexLabelA: {}, direction: {}, label: {}, vertexLabelB: {}",
                        sequence, LogType.of(log), getEdgeSchemas, ElapsedFrom.now()).with(vertexLabelA, direction.toString(), label, vertexLabelB).log();
            }
            return edgeSchemas;
        } catch (Exception ex) {
            thrownException = true;
            new LogMessage.Impl(this.verboseLogger, error, "failed getEdgeSchemas vertexLabelA: {}, direction: {}, label: {}, vertexLabelB: {}", sequence, LogType.of(failure), getEdgeSchemas, ElapsedFrom.now())
                    .with(vertexLabelA, direction.toString(), label, vertexLabelB, ex).log();
            throw ex;
        } finally {
            if (!thrownException) {
                new LogMessage.Impl(this.verboseLogger, trace, "finish getEdgeSchemas vertexLabelA: {}, direction: {}, label: {}, vertexLabelB: {}", sequence, LogType.of(success), getEdgeSchemas, ElapsedFrom.now())
                        .with(vertexLabelA, direction.toString(), label, vertexLabelB).log();
            }
        }
    }

    @Override
    public Optional<GraphElementPropertySchema> getPropertySchema(String name) {
        boolean thrownException = false;

        try {
            new LogMessage.Impl(this.verboseLogger, trace, "start getPropertySchema name: {}", sequence, LogType.of(start), getPropertySchema, ElapsedFrom.now())
                    .with(name).log();
            Optional<GraphElementPropertySchema> propertySchema = this.schemaProvider.getPropertySchema(name);
            if (!propertySchema.isPresent()) {
                new LogMessage.Impl(this.warnLogger, warn, "no property schema found for name: {}",
                        sequence, LogType.of(log), getPropertySchema, ElapsedFrom.now()).with(name).log();
            }
            return propertySchema;
        } catch (Exception ex) {
            thrownException = true;
            new LogMessage.Impl(this.verboseLogger, error, "failed getPropertySchema name: {}", sequence, LogType.of(failure), getPropertySchema, ElapsedFrom.now())
                    .with(name, ex).log();
            throw ex;
        } finally {
            if (!thrownException) {
                new LogMessage.Impl(this.verboseLogger, trace, "finish getPropertySchema name: {}", sequence, LogType.of(success), getPropertySchema, ElapsedFrom.now())
                        .with(name).log();
            }
        }
    }

    @Override
    public Iterable<String> getVertexLabels() {
        boolean thrownException = false;

        try {
            new LogMessage.Impl(this.verboseLogger, trace, "start getVertexLabels", sequence, LogType.of(start), getVertexLabels, ElapsedFrom.now()).log();
            Iterable<String> vertexLabels = this.schemaProvider.getVertexLabels();
            if (Stream.ofAll(vertexLabels).isEmpty()) {
                new LogMessage.Impl(this.warnLogger, warn, "no vertex labels found",
                        sequence, LogType.of(log), getVertexLabels, ElapsedFrom.now()).log();
            }
            return vertexLabels;
        } catch (Exception ex) {
            thrownException = true;
            new LogMessage.Impl(this.verboseLogger, error, "failed getVertexLabels", sequence, LogType.of(failure), getVertexLabels, ElapsedFrom.now())
                    .with(ex).log();
            throw ex;
        } finally {
            if (!thrownException) {
                new LogMessage.Impl(this.verboseLogger, trace, "finish getVertexLabels", sequence, LogType.of(success), getVertexLabels, ElapsedFrom.now()).log();
            }
        }
    }

    @Override
    public Iterable<String> getEdgeLabels() {
        boolean thrownException = false;

        try {
            new LogMessage.Impl(this.verboseLogger, trace, "start getEdgeLabels", sequence, LogType.of(start), getEdgeLabels, ElapsedFrom.now()).log();
            Iterable<String> vertexLabels = this.schemaProvider.getEdgeLabels();
            if (Stream.ofAll(vertexLabels).isEmpty()) {
                new LogMessage.Impl(this.warnLogger, warn, "no edge labels found",
                        sequence, LogType.of(log), getEdgeLabels, ElapsedFrom.now()).log();
            }
            return vertexLabels;
        } catch (Exception ex) {
            thrownException = true;
            new LogMessage.Impl(this.verboseLogger, error, "failed getEdgeLabels", sequence, LogType.of(failure), getEdgeLabels, ElapsedFrom.now())
                    .with(ex).log();
            throw ex;
        } finally {
            if (!thrownException) {
                new LogMessage.Impl(this.verboseLogger, trace, "finish getEdgeLabels", sequence, LogType.of(success), getEdgeLabels, ElapsedFrom.now()).log();
            }
        }
    }

    @Override
    public Iterable<String> getPropertyNames() {
        boolean thrownException = false;

        try {
            new LogMessage.Impl(this.verboseLogger, trace, "start getPropertyNames", sequence, LogType.of(start), getPropertyNames, ElapsedFrom.now()).log();
            Iterable<String> propertyNames = this.schemaProvider.getPropertyNames();
            if (Stream.ofAll(propertyNames).isEmpty()) {
                new LogMessage.Impl(this.warnLogger, warn, "no property names found",
                        sequence, LogType.of(log), getPropertyNames, ElapsedFrom.now()).log();
            }
            return propertyNames;
        } catch (Exception ex) {
            thrownException = true;
            new LogMessage.Impl(this.verboseLogger, error, "failed getPropertyNames", sequence, LogType.of(failure), getPropertyNames, ElapsedFrom.now())
                    .with(ex).log();
            throw ex;
        } finally {
            if (!thrownException) {
                new LogMessage.Impl(this.verboseLogger, trace, "finish getPropertyNames", sequence, LogType.of(success), getPropertyNames, ElapsedFrom.now()).log();
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
    private static MethodName.MDCWriter getPropertyNames = MethodName.of("getPropertyNames");

    private static LogMessage.MDCWriter sequence = Sequence.incr();
    //endregion
}
