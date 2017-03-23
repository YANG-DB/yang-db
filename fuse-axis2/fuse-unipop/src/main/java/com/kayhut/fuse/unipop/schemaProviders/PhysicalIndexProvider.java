package com.kayhut.fuse.unipop.schemaProviders;

import com.kayhut.fuse.unipop.schemaProviders.indexPartitions.IndexPartition;

/**
 * Created by benishue on 23-Mar-17.
 */
public interface PhysicalIndexProvider {

    Iterable<IndexPartition> getIndicesByVertexLabel(String vertexType);
    Iterable<IndexPartition> getIndicesByEdgeLabel(String edgeType);
}
