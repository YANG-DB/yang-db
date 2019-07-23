package com.yangdb.fuse.unipop.schemaProviders;

import org.apache.tinkerpop.gremlin.structure.Direction;

import java.util.Collections;
import java.util.Optional;

/**
 * Created by lior.perry on 29/03/2017.
 */
public class EmptyGraphElementSchemaProvider implements GraphElementSchemaProvider {
    public static EmptyGraphElementSchemaProvider instance = new EmptyGraphElementSchemaProvider();

    //region GraphElementSchemaProvider Implementation
    @Override
    public Iterable<GraphVertexSchema> getVertexSchemas(String label) {
        return Collections.emptyList();
    }

    @Override
    public Iterable<GraphEdgeSchema> getEdgeSchemas(String label) {
        return Collections.emptyList();
    }

    @Override
    public Iterable<GraphEdgeSchema> getEdgeSchemas(String vertexLabelA, String label) {
        return Collections.emptyList();
    }

    @Override
    public Iterable<GraphEdgeSchema> getEdgeSchemas(String vertexLabelA, Direction direction, String label) {
        return Collections.emptyList();
    }

    @Override
    public Iterable<GraphEdgeSchema> getEdgeSchemas(String vertexLabelA, Direction direction, String label, String vertexLabelB) {
        return Collections.emptyList();
    }

    @Override
    public Optional<GraphElementPropertySchema> getPropertySchema(String name) {
        return Optional.empty();
    }

    @Override
    public Iterable<String> getVertexLabels() {
        return Collections.emptyList();
    }

    @Override
    public Iterable<String> getEdgeLabels() {
        return Collections.emptyList();
    }

    @Override
    public Iterable<String> getPropertyNames() {
        return Collections.emptyList();
    }
    //endregion
}
