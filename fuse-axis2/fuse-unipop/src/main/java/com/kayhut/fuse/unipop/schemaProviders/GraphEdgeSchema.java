package com.kayhut.fuse.unipop.schemaProviders;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kayhut.fuse.unipop.schemaProviders.indexPartitions.IndexPartitions;
import javaslang.Tuple2;
import javaslang.collection.Stream;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__;
import org.apache.tinkerpop.gremlin.structure.Edge;
import org.apache.tinkerpop.gremlin.structure.T;

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

        class Impl implements Direction {
            //region Constructors
            public Impl(String field, Object outValue, Object inValue) {
                this.field = field;
                this.outValue = outValue;
                this.inValue = inValue;
            }
            //endregion

            //region Direction Implementation
            @Override
            public String getField() {
                return this.field;
            }

            @Override
            public Object getInValue() {
                return this.inValue;
            }

            @Override
            public Object getOutValue() {
                return this.outValue;
            }
            //endregion

            //region Fields
            private String field;
            private Object inValue;
            private Object outValue;
            //endregion
        }
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
        Optional<IndexPartitions> getIndexPartitions();

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
                this(idField, label, redundantPropertySchemas, routing, Optional.empty());
            }

            public Impl(String idField,
                        Optional<String> label,
                        Iterable<GraphRedundantPropertySchema> redundantPropertySchemas,
                        Optional<GraphElementRouting> routing,
                        Optional<IndexPartitions> indexPartitions) {
                this.idField = idField;
                this.label = label;
                this.redundantPropertySchemas = Stream.ofAll(redundantPropertySchemas)
                        .toJavaMap(property -> new Tuple2<>(property.getName(), property));
                this.routing = routing;
                this.indexPartitions = indexPartitions;
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

            @Override
            public Optional<IndexPartitions> getIndexPartitions() {
                return this.indexPartitions;
            }
            //endregion

            //region Fields
            private String idField;
            private Optional<String> label;
            private Map<String, GraphRedundantPropertySchema> redundantPropertySchemas;
            private Optional<GraphElementRouting> routing;
            private Optional<IndexPartitions> indexPartitions;
            //endregion
        }
    }

    class Impl extends GraphElementSchema.Impl implements GraphEdgeSchema {
        //region Constructors
        public Impl(String label,
                    GraphElementRouting routing) {
            this(label,
                    new GraphElementConstraint.Impl(__.has(T.label, label)),
                    Optional.empty(),
                    Optional.empty(),
                    Optional.empty(),
                    Optional.of(routing),
                    Optional.empty(),
                    Collections.emptyList());
        }

        public Impl(String label,
                    IndexPartitions indexPartitions) {
            this(label,
                    new GraphElementConstraint.Impl(__.has(T.label, label)),
                    Optional.empty(),
                    Optional.empty(),
                    Optional.empty(),
                    Optional.empty(),
                    Optional.of(indexPartitions),
                    Collections.emptyList());
        }

        public Impl(String label,
                    Optional<End> source,
                    Optional<End> destination,
                    Optional<Direction> direction,
                    GraphElementRouting routing) {
            this(label,
                    new GraphElementConstraint.Impl(__.has(T.label, label)),
                    source,
                    destination,
                    direction,
                    Optional.of(routing),
                    Optional.empty(),
                    Collections.emptyList());
        }

        public Impl(String label,
                    Optional<End> source,
                    Optional<End> destination,
                    Optional<Direction> direction,
                    IndexPartitions indexPartitions) {
            this(label,
                    new GraphElementConstraint.Impl(__.has(T.label, label)),
                    source,
                    destination,
                    direction,
                    Optional.empty(),
                    Optional.of(indexPartitions),
                    Collections.emptyList());
        }

        public Impl(String label,
                    Optional<End> source,
                    Optional<End> destination,
                    Optional<Direction> direction,
                    Optional<GraphElementRouting> routing,
                    Optional<IndexPartitions> indexPartitions) {
            this(label,
                    new GraphElementConstraint.Impl(__.has(T.label, label)),
                    source,
                    destination,
                    direction,
                    routing,
                    indexPartitions,
                    Collections.emptyList());
        }

        public Impl(String label,
                    GraphElementConstraint constraint,
                    Optional<End> source,
                    Optional<End> destination,
                    Optional<Direction> direction,
                    Optional<GraphElementRouting> routing,
                    Optional<IndexPartitions> indexPartitions,
                    Iterable<GraphElementPropertySchema> properties) {
            super(label, constraint, routing, indexPartitions, properties);
            this.source = source;
            this.destination = destination;
            this.direction = direction;
        }
        //endregion

        //region GraphEdgeSchema Implementation
        @Override
        public Optional<End> getSource() {
            return this.source;
        }

        @Override
        public Optional<End> getDestination() {
            return this.destination;
        }

        @Override
        public Optional<Direction> getDirection() {
            return this.direction;
        }
        //endregion

        //region Fields
        private Optional<End> source;
        private Optional<End> destination;
        private Optional<Direction> direction;
        //endregion
    }
}
