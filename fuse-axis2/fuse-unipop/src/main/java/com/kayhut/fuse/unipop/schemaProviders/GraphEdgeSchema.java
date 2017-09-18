package com.kayhut.fuse.unipop.schemaProviders;

import javaslang.Tuple2;
import javaslang.collection.Stream;
import org.apache.tinkerpop.gremlin.structure.Edge;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Created by r on 1/16/2015.
 */
public interface GraphEdgeSchema extends GraphElementSchema {
    default Class getSchemaElementType() {
        return Edge.class;
    }

    interface Direction {
        String getField();
        Object getInValue();
        Object getOutValue();
    }

    Optional<End> getSource();
    Optional<End> getDestination();

    Optional<Direction> getDirection();


    interface End {
        String getIdField();
        Optional<String> getLabel();
        Optional<GraphRedundantPropertySchema> getRedundantProperty(GraphElementPropertySchema property);
        Iterable<GraphRedundantPropertySchema> getRedundantProperties();
        Optional<GraphElementRouting> getRouting();

        class Impl implements End {
            //region Constructors
            public Impl(String idField,
                        Optional<String> label) {
                this(idField, label, Collections.emptyList(), Optional.empty());
            }

            public Impl(String idField,
                        Optional<String> label,
                        Iterable<GraphRedundantPropertySchema> redundantPropertySchemas) {
                this(idField, label, redundantPropertySchemas, Optional.empty());
            }

            public Impl(String idField,
                        Optional<String> label,
                        Iterable<GraphRedundantPropertySchema> redundantPropertySchemas,
                        Optional<GraphElementRouting> routing) {
                this.idField = idField;
                this.label = label;
                this.redundantPropertySchemas = Stream.ofAll(redundantPropertySchemas)
                        .toJavaMap(property -> new Tuple2<>(property.getName(), property));
                this.routing = routing;
            }
            //endregion

            //region End Implementation
            @Override
            public String getIdField() {
                return this.idField;
            }

            @Override
            public Optional<String> getLabel() {
                return this.label;
            }

            @Override
            public Optional<GraphRedundantPropertySchema> getRedundantProperty(GraphElementPropertySchema property) {
                GraphRedundantPropertySchema propertySchema = this.redundantPropertySchemas.get(property.getName());
                return propertySchema == null ? Optional.empty() : Optional.of(propertySchema);
            }

            @Override
            public Iterable<GraphRedundantPropertySchema> getRedundantProperties() {
                return this.redundantPropertySchemas.values();
            }

            @Override
            public Optional<GraphElementRouting> getRouting() {
                return this.routing;
            }
            //endregion

            //region Fields
            private String idField;
            private Optional<String> label;
            private Map<String, GraphRedundantPropertySchema> redundantPropertySchemas;
            private Optional<GraphElementRouting> routing;
            //endregion
        }
    }
}
