package com.kayhut.fuse.unipop.schemaProviders.indexPartitions;

import javaslang.collection.Stream;

import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

/**
 * Created by Roman on 11/05/2017.
 */
public class StaticIndexPartitions implements IndexPartitions {
    //region Constructors
    public StaticIndexPartitions(String...indices) {
        this(Stream.of(indices));
    }

    public StaticIndexPartitions(Iterable<String> indices) {
        this.indices = Stream.ofAll(indices).toJavaList();
    }
    //endregion

    //region IndexPartitions Implementation
    @Override
    public Optional<String> getPartitionField() {
        return Optional.empty();
    }

    @Override
    public Iterable<Partition> getPartitions() {
        return Collections.singletonList(() -> this.indices);
    }
    //endregion

    //region Fields
    private Iterable<String> indices;
    //endregion
}
