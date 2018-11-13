package com.kayhut.fuse.unipop.schemaProviders;

/*-
 * #%L
 * fuse-dv-unipop
 * %%
 * Copyright (C) 2016 - 2018 kayhut
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

import com.kayhut.fuse.unipop.schemaProviders.indexPartitions.IndexPartitions;
import javaslang.Tuple2;
import javaslang.collection.Stream;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__;
import org.apache.tinkerpop.gremlin.structure.Direction;
import org.apache.tinkerpop.gremlin.structure.Edge;
import org.apache.tinkerpop.gremlin.structure.T;

import java.util.*;

/**
 * Created by roman on 1/16/2015.
 */
public interface GraphEdgeSchema extends GraphElementSchema {
    enum Application {
        start,
        endA,
        endB
    };

    default Class getSchemaElementType() {
        return Edge.class;
    }

    default Set<Application> getApplications() {
        return new HashSet<>(Arrays.asList(Application.start, Application.endA, Application.endB));
    }

    Optional<End> getEndA();
    Optional<End> getEndB();
    Optional<DirectionSchema> getDirectionSchema();
    Direction getDirection();

    interface DirectionSchema {
        String getField();
        Object getInValue();
        Object getOutValue();

        class Impl implements DirectionSchema {
            //region Constructors
            public Impl(String field, Object outValue, Object inValue) {
                this.field = field;
                this.outValue = outValue;
                this.inValue = inValue;
            }
            //endregion

            //region DirectionSchema Implementation
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
                    Direction.OUT,
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
                    Direction.OUT,
                    Optional.empty(),
                    Optional.empty(),
                    Optional.of(indexPartitions),
                    Collections.emptyList());
        }

        public Impl(String label,
                    Optional<End> endA,
                    Optional<End> endB,
                    Direction direction,
                    Optional<DirectionSchema> directionSchema,
                    GraphElementRouting routing) {
            this(label,
                    new GraphElementConstraint.Impl(__.has(T.label, label)),
                    endA,
                    endB,
                    direction,
                    directionSchema,
                    Optional.of(routing),
                    Optional.empty(),
                    Collections.emptyList());
        }

        public Impl(String label,
                    Optional<End> endA,
                    Optional<End> endB,
                    Direction direction,
                    Optional<DirectionSchema> directionSchema,
                    IndexPartitions indexPartitions) {
            this(label,
                    new GraphElementConstraint.Impl(__.has(T.label, label)),
                    endA,
                    endB,
                    direction,
                    directionSchema,
                    Optional.empty(),
                    Optional.of(indexPartitions),
                    Collections.emptyList());
        }

        public Impl(String label,
                    Optional<End> endA,
                    Optional<End> endB,
                    Direction direction,
                    Optional<DirectionSchema> directionSchema,
                    Optional<GraphElementRouting> routing,
                    Optional<IndexPartitions> indexPartitions) {
            this(label,
                    new GraphElementConstraint.Impl(__.has(T.label, label)),
                    endA,
                    endB,
                    direction,
                    directionSchema,
                    routing,
                    indexPartitions,
                    Collections.emptyList());
        }

        public Impl(String label,
                    GraphElementConstraint constraint,
                    Optional<End> endA,
                    Optional<End> endB,
                    Direction direction,
                    Optional<DirectionSchema> directionSchema,
                    Optional<GraphElementRouting> routing,
                    Optional<IndexPartitions> indexPartitions,
                    Iterable<GraphElementPropertySchema> properties) {
            this(label,
                    constraint,
                    endA,
                    endB,
                    direction,
                    directionSchema,
                    routing,
                    indexPartitions,
                    properties,
                    Stream.of(Application.endA, Application.endB, Application.start).toJavaSet());
        }

        public Impl(String label,
                    GraphElementConstraint constraint,
                    Optional<End> endA,
                    Optional<End> endB,
                    Direction direction,
                    Optional<DirectionSchema> directionSchema,
                    Optional<GraphElementRouting> routing,
                    Optional<IndexPartitions> indexPartitions,
                    Iterable<GraphElementPropertySchema> properties,
                    Set<Application> applications) {
            super(label, constraint, routing, indexPartitions, properties);
            this.endA = endA;
            this.endB = endB;
            this.direction = direction;
            this.directionSchema = directionSchema;
            this.applications = applications;
        }


        //endregion

        //region GraphEdgeSchema Implementation
        @Override
        public Optional<End> getEndA() {
            return this.endA;
        }

        @Override
        public Optional<End> getEndB() {
            return this.endB;
        }

        @Override
        public Optional<DirectionSchema> getDirectionSchema() {
            return this.directionSchema;
        }

        @Override
        public Direction getDirection() {
            return this.direction;
        }

        @Override
        public Set<Application> getApplications() {
            return this.applications;
        }
        //endregion

        //region Fields
        private Optional<End> endA;
        private Optional<End> endB;
        private Optional<DirectionSchema> directionSchema;
        private Direction direction;
        private Set<Application> applications;
        //endregion
    }
}
