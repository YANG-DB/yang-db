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
    public Optional<GraphVertexSchema> getVertexSchema(String type) {
        return Optional.empty();
    }

    @Override
    public Optional<GraphEdgeSchema> getEdgeSchema(String type) {
        return Optional.empty();
    }

    @Override
    public Optional<Iterable<GraphEdgeSchema>> getEdgeSchemas(String type) {
        return Optional.empty();
    }

    @Override
    public Iterable<String> getVertexTypes() {
        return Collections.emptyList();
    }

    @Override
    public Iterable<String> getEdgeTypes() {
        return Collections.emptyList();
    }
    //endregion
}
