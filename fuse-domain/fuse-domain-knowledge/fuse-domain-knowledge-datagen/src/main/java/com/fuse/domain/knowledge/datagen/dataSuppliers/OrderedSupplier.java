package com.fuse.domain.knowledge.datagen.dataSuppliers;

import java.util.Iterator;
import java.util.function.Supplier;

/**
 * Created by Roman on 6/23/2018.
 */
public class OrderedSupplier<T> implements Supplier<T> {
    //region Constructors
    public OrderedSupplier(Iterable<T> items) {
        this.iterator = items.iterator();
    }
    //endregion

    //region Supplier Implementation
    @Override
    public T get() {
        return iterator.next();
    }
    //endregion

    //region
    private Iterator<T> iterator;
    //endregion
}
