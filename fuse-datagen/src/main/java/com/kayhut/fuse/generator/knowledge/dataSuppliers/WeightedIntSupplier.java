package com.kayhut.fuse.generator.knowledge.dataSuppliers;

import javaslang.collection.Stream;

import java.util.Map;

/**
 * Created by Roman on 6/23/2018.
 */
public class WeightedIntSupplier extends RandomDataSupplier<Integer> {
    //region Constructors
    public WeightedIntSupplier(Map<Integer, Integer> valueCounts) {
        long total = Stream.ofAll(valueCounts.values()).sum().longValue();
        Stream.ofAll(valueCounts.entrySet())
                .sortBy(entry -> (double)entry.getValue() / (double)total);

    }
    //endregion

    //region RandomDataSupplier Implementation
    @Override
    public Integer get() {
        return null;
    }
    //endregion

    //region Fields
    //endregion
}
