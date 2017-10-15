package com.kayhut.fuse.unipop.schemaProviders;

import javaslang.Tuple2;
import javaslang.collection.Stream;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Created by r on 1/16/2015.
 */
public interface GraphElementSchemaProvider {
    Optional<GraphVertexSchema> getVertexSchema(String label);

    Optional<GraphEdgeSchema> getEdgeSchema(String label);
    Iterable<GraphEdgeSchema> getEdgeSchemas(String label);

    Optional<GraphElementPropertySchema> getPropertySchema(String name);

    Iterable<String> getVertexLabels();
    Iterable<String> getEdgeLabels();

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
                    .toJavaMap(vertexSchema -> new Tuple2<>(vertexSchema.getLabel(), vertexSchema));
            this.edgeSchemas = Stream.ofAll(edgeSchemas)
                    .groupBy(GraphElementSchema::getLabel)
                    .toJavaMap(grouping -> new Tuple2<>(grouping._1(), grouping._2().toJavaList()));
            this.propertySchemas = Stream.ofAll(propertySchemas)
                    .toJavaMap(propertySchema -> new Tuple2<>(propertySchema.getName(), propertySchema));
        }
        //endregion

        //region GraphElementSchemaProvider Implementation
        @Override
        public Optional<GraphVertexSchema> getVertexSchema(String label) {
            return Optional.ofNullable(this.vertexSchemas.get(label));
        }

        @Override
        public Optional<GraphEdgeSchema> getEdgeSchema(String label) {
            List<GraphEdgeSchema> edgeSchemas = this.edgeSchemas.get(label);
            if (edgeSchemas == null || edgeSchemas.isEmpty()) {
                return Optional.empty();
            }

            return Optional.of(edgeSchemas.get(0));
        }

        @Override
        public Iterable<GraphEdgeSchema> getEdgeSchemas(String label) {
            List<GraphEdgeSchema> edgeSchemas = this.edgeSchemas.get(label);
            if (edgeSchemas == null) {
                return Collections.emptyList();
            }

            return edgeSchemas;
        }

        @Override
        public Optional<GraphElementPropertySchema> getPropertySchema(String name) {
            return Optional.ofNullable(this.propertySchemas.get(name));
        }

        @Override
        public Iterable<String> getVertexLabels() {
            return this.vertexSchemas.keySet();
        }

        @Override
        public Iterable<String> getEdgeLabels() {
            return this.edgeSchemas.keySet();
        }
        //endregion

        //region Fields
        private Map<String, GraphVertexSchema> vertexSchemas;
        private Map<String, List<GraphEdgeSchema>> edgeSchemas;
        private Map<String, GraphElementPropertySchema> propertySchemas;
        //endregion
    }
}
