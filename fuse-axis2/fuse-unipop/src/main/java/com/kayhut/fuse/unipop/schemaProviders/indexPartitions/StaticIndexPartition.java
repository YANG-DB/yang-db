package com.kayhut.fuse.unipop.schemaProviders.indexPartitions;

import javaslang.collection.Stream;

/**
 * Created by Roman on 11/05/2017.
 */
public class StaticIndexPartition implements IndexPartition {
    //region Constructors
    public StaticIndexPartition(Iterable<String> indices) {
        this.indices = Stream.ofAll(indices).toJavaSet();
    }
    //endregion

    //region IndexPartition Implementation
    @Override
    public Iterable<String> getIndices() {
        return this.indices;
    }
    //endregion

    //region Fields
    private Iterable<String> indices;
    //endregion
}
