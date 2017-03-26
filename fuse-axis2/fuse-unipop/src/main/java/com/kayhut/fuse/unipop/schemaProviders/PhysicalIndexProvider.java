package com.kayhut.fuse.unipop.schemaProviders;

import com.kayhut.fuse.unipop.schemaProviders.indexPartitions.IndexPartition;
import org.apache.tinkerpop.gremlin.structure.Element;

/**
 * Created by benishue on 23-Mar-17.
 */
public interface PhysicalIndexProvider {
    enum ElementType {
        vertex,
        edge
    }

    Iterable<IndexPartition> getIndexPartitionsByLabel(String label, ElementType elementType);

}
