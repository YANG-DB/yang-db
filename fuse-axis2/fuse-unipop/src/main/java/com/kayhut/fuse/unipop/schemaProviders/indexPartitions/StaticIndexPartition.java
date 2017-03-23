package com.kayhut.fuse.unipop.schemaProviders.indexPartitions;

public interface StaticIndexPartition extends IndexPartition {
    Iterable<String> getIndices();
}
