package com.kayhut.fuse.unipop.schemaProviders;

import com.kayhut.fuse.unipop.schemaProviders.indexPartitions.IndexPartition;
import com.kayhut.fuse.unipop.structure.*;


/**
 * Created by benishue on 23-Mar-17.
 */
public interface PhysicalIndexProvider {
    Iterable<IndexPartition> getIndexPartitionsByLabel(String label, ElementType elementType);
}
