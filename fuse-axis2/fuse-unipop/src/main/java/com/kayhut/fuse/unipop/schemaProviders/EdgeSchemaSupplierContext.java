package com.kayhut.fuse.unipop.schemaProviders;

import com.kayhut.fuse.model.query.Rel;

import java.util.Set;

public interface EdgeSchemaSupplierContext {
    Set<String> getLabels();
    Rel.Direction getDirection();
    String getVertexLabel();
    GraphElementSchemaProvider getGraphElementSchemaProvider();
}
