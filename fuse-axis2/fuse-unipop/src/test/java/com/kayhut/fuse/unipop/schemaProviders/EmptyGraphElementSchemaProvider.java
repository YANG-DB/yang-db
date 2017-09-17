package com.kayhut.fuse.unipop.schemaProviders;

import java.util.Collections;
import java.util.Optional;

/**
 * Created by User on 29/03/2017.
 */
public class EmptyGraphElementSchemaProvider implements GraphElementSchemaProvider {
    public static EmptyGraphElementSchemaProvider instance = new EmptyGraphElementSchemaProvider();

    //region GraphElementSchemaProvider Implementation
    @Override
    public Optional<GraphVertexSchema> getVertexSchema(String label) {
        return Optional.empty();
    }

    @Override
    public Optional<GraphEdgeSchema> getEdgeSchema(String label) {
        return Optional.empty();
    }

    @Override
    public Iterable<GraphEdgeSchema> getEdgeSchemas(String label) {
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
    //endregion
}
