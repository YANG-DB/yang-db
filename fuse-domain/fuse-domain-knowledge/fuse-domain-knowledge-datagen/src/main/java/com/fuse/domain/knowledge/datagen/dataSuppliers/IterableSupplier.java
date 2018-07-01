package com.fuse.domain.knowledge.datagen.dataSuppliers;

import javaslang.collection.Stream;

import java.util.function.Supplier;

/**
 * Created by Roman on 6/23/2018.
 */
public class IterableSupplier<T> extends RandomDataSupplier<Iterable<T>> {
    //region Constructors
    public IterableSupplier(Supplier<T> itemSupplier, Supplier<Integer> numItemsSupplier) {
        this(itemSupplier, numItemsSupplier, 0);
    }

    public IterableSupplier(Supplier<T> itemSupplier, Supplier<Integer> numItemsSupplier, long seed) {
        super(seed);
        this.itemSupplier = itemSupplier;
        this.numItemsSupplier = numItemsSupplier;
    }
    //endregion

    //region RandomDataSupplier<Iterable<String>> Implementation
    @Override
    public Iterable<T> get() {
        return Stream.fill(this.numItemsSupplier.get(), this.itemSupplier).toJavaList();
    }
    //endregion

    //region Fields
    private Supplier<T> itemSupplier;
    private Supplier<Integer> numItemsSupplier;
    //endregion
}
