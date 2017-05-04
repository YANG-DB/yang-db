package com.kayhut.fuse.unipop.schemaProviders;

import java.util.Optional;

/**
 * Created by r on 1/16/2015.
 */
public interface GraphElementSchemaProvider {
    Optional<GraphVertexSchema> getVertexSchema(String type);
    Optional<GraphEdgeSchema> getEdgeSchema(String type);
    Optional<Iterable<GraphEdgeSchema>> getEdgeSchemas(String type);

    Iterable<String> getVertexTypes();
    Iterable<String> getEdgeTypes();
}
