package com.kayhut.fuse.unipop.schemaProviders;

import com.kayhut.fuse.unipop.schemaProviders.indexPartitions.IndexPartitions;
import javaslang.Tuple2;
import javaslang.collection.Stream;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__;
import org.apache.tinkerpop.gremlin.structure.Edge;
import org.apache.tinkerpop.gremlin.structure.T;

import java.util.*;

/**
 * Created by r on 1/16/2015.
 */
public interface GraphEdgeSchema extends GraphElementSchema {
    enum Application {
        start,
        source,
        destination
    };

    default Class getSchemaElementType() {
        return Edge.class;
    }

    default Set<Application> getApplications() {
        return new HashSet<>(Arrays.asList(Application.start, Application.source, Application.destination));
    }

    Optional<End> getSource();
    Optional<End> getDestination();
    Optional<Direction> getDirection();

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


    interface End {
        Iterable<String> getIdFields();
        Optional<String> getLabel();
        Optional<GraphRedundantPropertySchema> getRedundantProperty(GraphElementPropertySchema property);
        Iterable<GraphRedundantPropertySchema> getRedundantProperties();
        Optional<GraphElementRouting> getRouting();
        Optional<IndexPartitions> getIndexPartitions();

        class Impl implements End {
            //region Constructors
            public Impl(Iterable<String> idFields,
                        Optional<String> label) {
                this(idFields, label, Collections.emptyList(), Optional.empty());
            }

            public Impl(Iterable<String> idFields,
                        Optional<String> label,
                        Iterable<GraphRedundantPropertySchema> redundantPropertySchemas) {
                this(idFields, label, redundantPropertySchemas, Optional.empty());
            }

            public Impl(Iterable<String> idFields,
                        Optional<String> label,
                        Iterable<GraphRedundantPropertySchema> redundantPropertySchemas,
                        Optional<GraphElementRouting> routing) {
                this(idFields, label, redundantPropertySchemas, routing, Optional.empty());
            }

            public Impl(Iterable<String> idFields,
                        Optional<String> label,
                        Iterable<GraphRedundantPropertySchema> redundantPropertySchemas,
                        Optional<GraphElementRouting> routing,
                        Optional<IndexPartitions> indexPartitions) {
                this.idFields = idFields;
                this.label = label;
                this.redundantPropertySchemas = Stream.ofAll(redundantPropertySchemas)
                        .toJavaMap(property -> new Tuple2<>(property.getName(), property));
                this.routing = routing;
                this.indexPartitions = indexPartitions;
            }
            //endregion

            //region End Implementation
            @Override
            public Iterable<String> getIdFields() {
                return this.idFields;
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
            private Iterable<String> idFields;
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
            this(label,
                    constraint,
                    source,
                    destination,
                    direction,
                    routing,
                    indexPartitions,
                    properties,
                    Stream.of(Application.source, Application.destination, Application.start).toJavaSet());
        }

        public Impl(String label,
                    GraphElementConstraint constraint,
                    Optional<End> source,
                    Optional<End> destination,
                    Optional<Direction> direction,
                    Optional<GraphElementRouting> routing,
                    Optional<IndexPartitions> indexPartitions,
                    Iterable<GraphElementPropertySchema> properties,
                    Set<Application> applications) {
            super(label, constraint, routing, indexPartitions, properties);
            this.source = source;
            this.destination = destination;
            this.direction = direction;
            this.applications = applications;
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

        @Override
        public Set<Application> getApplications() {
            return this.applications;
        }
        //endregion

        //region Fields
        private Optional<End> source;
        private Optional<End> destination;
        private Optional<Direction> direction;
        private Set<Application> applications;
        //endregion
    }
}
