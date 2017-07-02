package com.kayhut.fuse.unipop.schemaProviders;

import org.apache.tinkerpop.gremlin.structure.Vertex;

/**
 * Created by r on 1/16/2015.
 */
public interface GraphVertexSchema extends GraphElementSchema {
    default Class getSchemaElementType() {
        return Vertex.class;
    }
}
