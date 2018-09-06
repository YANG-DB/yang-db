package com.fuse.domain.knowledge.datagen.dataSuppliers;

import java.util.HashSet;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.function.Supplier;

public class DistinctValueSupplier<T> implements Supplier<T> {
    //region Constructors
    public DistinctValueSupplier(Supplier<T> valueSupplier, int maxIterations) {
        this.valueSupplier = valueSupplier;
        this.distinctValues = new HashSet<>();
        this.maxIterations = maxIterations;
    }
    //endregion

    //region Supplier Implementation
    @Override
    public T get() {
        T value = this.valueSupplier.get();
        int iterations = 0;
        while(this.distinctValues.contains(value) && iterations < this.maxIterations) {
            value = this.valueSupplier.get();
            iterations++;
        }

        if (this.distinctValues.contains(value)) {
            throw new NoSuchElementException();
        }

        return value;
    }
    //endregion

    //region Fields
    private Set<T> distinctValues;
    private Supplier<T> valueSupplier;
    private int maxIterations;
    //endregion
}
