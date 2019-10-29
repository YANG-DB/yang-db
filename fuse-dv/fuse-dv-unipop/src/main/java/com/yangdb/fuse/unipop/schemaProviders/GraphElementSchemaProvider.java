package com.yangdb.fuse.unipop.schemaProviders;

/*-
 * #%L
 * fuse-dv-unipop
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

import javaslang.Tuple2;
import javaslang.collection.Stream;
import org.apache.tinkerpop.gremlin.structure.Direction;

import java.util.*;

import static com.yangdb.fuse.unipop.schemaProviders.GraphEdgeSchema.Application.endB;

/**
 * Created by roman on 1/16/2015.
 */
public interface GraphElementSchemaProvider {
    Iterable<GraphVertexSchema> getVertexSchemas(String label);

    Iterable<GraphEdgeSchema> getEdgeSchemas(String label);
    Iterable<GraphEdgeSchema> getEdgeSchemas(String vertexLabelA, String label);
    Iterable<GraphEdgeSchema> getEdgeSchemas(String vertexLabelA, Direction direction, String label);
    Iterable<GraphEdgeSchema> getEdgeSchemas(String vertexLabelA, Direction direction, String label, String vertexLabelB);

    Optional<GraphElementPropertySchema> getPropertySchema(String name);

    Iterable<String> getVertexLabels();
    Iterable<String> getEdgeLabels();
    Iterable<String> getPropertyNames();

    default Iterable<GraphVertexSchema> getVertexSchemas() {
        return Stream.ofAll(getVertexLabels())
                .flatMap(this::getVertexSchemas)
                .toJavaList();
    }

    default Iterable<GraphEdgeSchema> getEdgeSchemas() {
        return Stream.ofAll(getEdgeLabels())
                .flatMap(this::getEdgeSchemas)
                .toJavaList();
    }

    default Iterable<GraphElementPropertySchema> getPropertySchemas() {
        return Stream.ofAll(getPropertyNames())
                .map(this::getPropertySchema)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .toJavaList();
    }

    class Impl implements GraphElementSchemaProvider {
        //region Constructors
        public Impl(Iterable<GraphVertexSchema> vertexSchemas,
                    Iterable<GraphEdgeSchema> edgeSchemas) {
            this(vertexSchemas, edgeSchemas, Collections.emptyList());
        }

        public Impl(Iterable<GraphVertexSchema> vertexSchemas,
                    Iterable<GraphEdgeSchema> edgeSchemas,
                    Iterable<GraphElementPropertySchema> propertySchemas) {
            this.vertexSchemas = Stream.ofAll(vertexSchemas)
                    .groupBy(GraphElementSchema::getLabel)
                    .toJavaMap(grouping -> new Tuple2<>(grouping._1(), grouping._2().toJavaList()));

            this.edgeSchemas = complementEdgeSchemas(edgeSchemas);

            this.propertySchemas = Stream.ofAll(propertySchemas)
                    .toJavaMap(propertySchema -> new Tuple2<>(propertySchema.getName(), propertySchema));

            this.vertexLabels =
                    Stream.ofAll(this.vertexSchemas.values())
                    .flatMap(vertexSchemas1 -> vertexSchemas1)
                    .map(GraphElementSchema::getLabel)
                    .toJavaSet();

            this.edgeLabels =
                    Stream.ofAll(this.edgeSchemas.values())
                            .flatMap(edgeSchemas1 -> edgeSchemas1)
                            .map(GraphElementSchema::getLabel)
                            .toJavaSet();

            this.propertyNames =
                    Stream.ofAll(this.propertySchemas.values())
                    .map(GraphElementPropertySchema::getName)
                    .toJavaSet();
        }
        //endregion

        //region GraphElementSchemaProvider Implementation
        @Override
        public Iterable<GraphVertexSchema> getVertexSchemas(String label) {
            return Optional.ofNullable(this.vertexSchemas.get(label)).orElseGet(Collections::emptyList);
        }

        @Override
        public Iterable<GraphEdgeSchema> getEdgeSchemas(String label) {
            return Optional.ofNullable(this.edgeSchemas.get(edgeSchemaKey(label))).orElseGet(Collections::emptyList);
        }

        @Override
        public Iterable<GraphEdgeSchema> getEdgeSchemas(String vertexLabelA, String label) {
            return Optional.ofNullable(this.edgeSchemas.get(edgeSchemaKey(vertexLabelA, label))).orElseGet(Collections::emptyList);
        }

        @Override
        public Iterable<GraphEdgeSchema> getEdgeSchemas(String vertexLabelA, Direction direction, String label) {
            //this.edgeSchemas.entrySet().stream().filter(p->!p.getValue().isEmpty()).filter(p->p.getKey().contains("relatedEntity")).collect(Collectors.toList())
            return Optional.ofNullable(this.edgeSchemas.get(edgeSchemaKey(vertexLabelA, direction.toString(), label))).orElseGet(Collections::emptyList);
        }

        @Override
        public Iterable<GraphEdgeSchema> getEdgeSchemas(String vertexLabelA, Direction direction, String label, String vertexLabelB) {
            return Optional.ofNullable(this.edgeSchemas.get(edgeSchemaKey(vertexLabelA, direction.toString(), label, vertexLabelB))).orElseGet(Collections::emptyList);
        }

        @Override
        public Optional<GraphElementPropertySchema> getPropertySchema(String name) {
            return Optional.ofNullable(this.propertySchemas.get(name));
        }

        @Override
        public Iterable<String> getVertexLabels() {
            return this.vertexLabels;
        }

        @Override
        public Iterable<String> getEdgeLabels() {
            return this.edgeLabels;
        }

        @Override
        public Iterable<String> getPropertyNames() {
            return this.propertyNames;
        }
        //endregion

        //region

        //region Protected Methods
        protected Map<String, List<GraphEdgeSchema>> complementEdgeSchemas(Iterable<GraphEdgeSchema> edgeSchemas) {
            Iterable<GraphEdgeSchema> complementedSchemas =
                    Stream.ofAll(edgeSchemas)
                    .flatMap(edgeSchema -> complementEdgeSchema(edgeSchema))
                    .toJavaList();

            Map<String, List<GraphEdgeSchema>> labelSchemas =
                    Stream.ofAll(edgeSchemas) // temporary - should be complementedSchemas
                            .groupBy(edgeSchema -> edgeSchemaKey(edgeSchema.getLabel()))
                            .toJavaMap(grouping -> new Tuple2<>(grouping._1(), grouping._2().toJavaList()));

            Map<String, List<GraphEdgeSchema>> labelALabelSchemas =
                    Stream.ofAll(complementedSchemas)
                            .filter(edgeSchema -> edgeSchema.getEndA().isPresent())
                            .filter(edgeSchema -> edgeSchema.getEndA().get().getLabel().isPresent())
                            .groupBy(edgeSchema -> edgeSchemaKey(edgeSchema.getEndA().get().getLabel().get(), edgeSchema.getLabel()))
                            .toJavaMap(grouping -> new Tuple2<>(grouping._1(), grouping._2().toJavaList()));

            Map<String, List<GraphEdgeSchema>> labelADirLabelSchemas =
                    Stream.ofAll(complementedSchemas)
                            .filter(edgeSchema -> edgeSchema.getEndA().isPresent())
                            .filter(edgeSchema -> edgeSchema.getEndA().get().getLabel().isPresent())
                            .groupBy(edgeSchema -> edgeSchemaKey(
                                    edgeSchema.getEndA().get().getLabel().get(),
                                    edgeSchema.getDirection().toString(),
                                    edgeSchema.getLabel()))
                            .toJavaMap(grouping -> new Tuple2<>(grouping._1(), grouping._2().toJavaList()));

            Map<String, List<GraphEdgeSchema>> labelADirLabelLabelBSchemas =
                    Stream.ofAll(complementedSchemas)
                            .filter(edgeSchema -> edgeSchema.getEndA().isPresent())
                            .filter(edgeSchema -> edgeSchema.getEndA().get().getLabel().isPresent())
                            .filter(edgeSchema -> edgeSchema.getEndB().isPresent())
                            .filter(edgeSchema -> edgeSchema.getEndB().get().getLabel().isPresent())
                            .groupBy(edgeSchema -> edgeSchemaKey(
                                    edgeSchema.getEndA().get().getLabel().get(),
                                    edgeSchema.getDirection().toString(),
                                    edgeSchema.getLabel(),
                                    edgeSchema.getEndB().get().getLabel().get()))
                            .toJavaMap(grouping -> new Tuple2<>(grouping._1(), grouping._2().toJavaList()));

            Map<String, List<GraphEdgeSchema>> complementedSchemaMap = new HashMap<>();
            complementedSchemaMap.putAll(labelSchemas);
            complementedSchemaMap.putAll(labelALabelSchemas);
            complementedSchemaMap.putAll(labelADirLabelSchemas);
            complementedSchemaMap.putAll(labelADirLabelLabelBSchemas);
            return complementedSchemaMap;
        }

        protected Iterable<GraphEdgeSchema> complementEdgeSchema(GraphEdgeSchema edgeSchema) {
            List<GraphEdgeSchema> edgeSchemas = new ArrayList<>();
            if (edgeSchema.getApplications().contains(GraphEdgeSchema.Application.endA)) {
                edgeSchemas.add(new GraphEdgeSchema.Impl(
                        edgeSchema.getLabel(),
                        edgeSchema.getConstraint(),
                        edgeSchema.getEndA(),
                        edgeSchema.getEndB(),
                        edgeSchema.getDirection(),
                        edgeSchema.getDirectionSchema(),
                        edgeSchema.getRouting(),
                        edgeSchema.getIndexPartitions(),
                        edgeSchema.getProperties(),
                        edgeSchema.getApplications()
                ));
            }

            if (edgeSchema.getApplications().contains(endB)) {
                edgeSchemas.add(new GraphEdgeSchema.Impl(
                        edgeSchema.getLabel(),
                        edgeSchema.getConstraint(),
                        edgeSchema.getEndB(),
                        edgeSchema.getEndA(),
                        edgeSchema.getDirection().equals(Direction.OUT) ? Direction.IN : Direction.OUT,
                        edgeSchema.getDirectionSchema(),
                        edgeSchema.getRouting(),
                        edgeSchema.getIndexPartitions(),
                        edgeSchema.getProperties(),
                        edgeSchema.getApplications()
                ));
            }

            return edgeSchemas;
        }

        protected String edgeSchemaKey(String...parts) {
            return String.join(".", Stream.of(parts));
        }
        //endregion

        //region Fields
        protected Map<String, List<GraphVertexSchema>> vertexSchemas;
        protected Map<String, List<GraphEdgeSchema>> edgeSchemas;
        protected Map<String, GraphElementPropertySchema> propertySchemas;
        protected Iterable<String> vertexLabels;
        protected Iterable<String> edgeLabels;
        protected Iterable<String> propertyNames;
        //endregion
    }

    class Cached extends GraphElementSchemaProvider.Impl {
        //region Constructors
        public Cached(GraphElementSchemaProvider schemaProvider) {
            super(Collections.emptyList(), Collections.emptyList());

            this.schemaProvider = schemaProvider;

            this.vertexLabels = this.schemaProvider.getVertexLabels();
            this.edgeLabels = this.schemaProvider.getEdgeLabels();
            this.propertyNames = this.schemaProvider.getPropertyNames();

            initializeVertexSchemas();
            initializeEdgeSchemas();
            initializePropertySchemas();
        }
        //endregion

        //region Private Methods
        private void initializeVertexSchemas() {
            this.vertexSchemas = new HashMap<>();

            for(String vertexLabel : this.vertexLabels) {
                this.vertexSchemas.put(vertexLabel, Stream.ofAll(this.schemaProvider.getVertexSchemas(vertexLabel)).toJavaList());
            }
        }

        private void initializeEdgeSchemas() {
            this.edgeSchemas = new HashMap<>();

            for(String edgeLabel : this.edgeLabels) {
                this.edgeSchemas.put(this.edgeSchemaKey(edgeLabel),
                        Stream.ofAll(this.schemaProvider.getEdgeSchemas(edgeLabel)).toJavaList());

                for(String vertexLabelA : this.vertexLabels) {
                    this.edgeSchemas.put(this.edgeSchemaKey(vertexLabelA, edgeLabel),
                            Stream.ofAll(this.schemaProvider.getEdgeSchemas(vertexLabelA, edgeLabel)).toJavaList());

                    for(Direction direction : Arrays.asList(Direction.OUT, Direction.IN)) {
                        this.edgeSchemas.put(this.edgeSchemaKey(vertexLabelA, direction.toString(), edgeLabel),
                                Stream.ofAll(this.schemaProvider.getEdgeSchemas(vertexLabelA, direction, edgeLabel)).toJavaList());

                        for(String vertexLabelB : this.vertexLabels) {
                            this.edgeSchemas.put(this.edgeSchemaKey(vertexLabelA, direction.toString(), edgeLabel, vertexLabelB),
                                    Stream.ofAll(this.schemaProvider.getEdgeSchemas(vertexLabelA, direction, edgeLabel, vertexLabelB)).toJavaList());
                        }
                    }
                }
            }
        }

        private void initializePropertySchemas() {
            this.propertySchemas = new HashMap<>();

            for(String propertyName : this.propertyNames) {
                this.propertySchemas.put(propertyName, this.schemaProvider.getPropertySchema(propertyName).get());
            }
        }
        //endregion

        //region Fields
        private GraphElementSchemaProvider schemaProvider;
        //endregion
    }
}
