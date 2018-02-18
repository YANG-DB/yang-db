package com.kayhut.fuse.unipop.schemaProviders;

import javaslang.Tuple2;
import javaslang.collection.Stream;
import org.apache.tinkerpop.gremlin.structure.Direction;

import java.util.*;

import static com.kayhut.fuse.unipop.schemaProviders.GraphEdgeSchema.Application.endB;

/**
 * Created by r on 1/16/2015.
 */
public interface GraphElementSchemaProvider {
    Iterable<GraphVertexSchema> getVertexSchemas(String label);

    //Optional<GraphEdgeSchema> getEdgeSchema(String label);

    Iterable<GraphEdgeSchema> getEdgeSchemas(String label);
    Iterable<GraphEdgeSchema> getEdgeSchemas(String vertexLabelA, String label);
    Iterable<GraphEdgeSchema> getEdgeSchemas(String vertexLabelA, Direction direction, String label);
    Iterable<GraphEdgeSchema> getEdgeSchemas(String vertexLabelA, Direction direction, String label, String vertexLabelB);

    Optional<GraphElementPropertySchema> getPropertySchema(String name);

    Iterable<String> getVertexLabels();
    Iterable<String> getEdgeLabels();

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
            return Stream.ofAll(this.vertexSchemas.values())
                    .flatMap(vertexSchemas ->vertexSchemas)
                    .map(GraphElementSchema::getLabel)
                    .toJavaSet();
        }

        @Override
        public Iterable<String> getEdgeLabels() {
            return Stream.ofAll(this.edgeSchemas.values())
                    .flatMap(edgeSchemas -> edgeSchemas)
                    .map(GraphElementSchema::getLabel)
                    .toJavaSet();
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
        private Map<String, List<GraphVertexSchema>> vertexSchemas;
        private Map<String, List<GraphEdgeSchema>> edgeSchemas;
        private Map<String, GraphElementPropertySchema> propertySchemas;
        //endregion
    }
}
