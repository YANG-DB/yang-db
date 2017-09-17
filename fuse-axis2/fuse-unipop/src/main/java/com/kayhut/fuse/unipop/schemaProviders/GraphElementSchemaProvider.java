package com.kayhut.fuse.unipop.schemaProviders;

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
}
