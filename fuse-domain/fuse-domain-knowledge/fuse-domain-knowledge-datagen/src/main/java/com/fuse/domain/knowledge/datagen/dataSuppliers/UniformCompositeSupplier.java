package com.fuse.domain.knowledge.datagen.dataSuppliers;

import javaslang.collection.Stream;

import java.util.List;
import java.util.function.Supplier;

/**
 * Created by Roman on 6/24/2018.
 */
public class UniformCompositeSupplier<T> extends RandomDataSupplier<T> {
    //region Constructors
    public UniformCompositeSupplier(Iterable<Supplier<T>> suppliers) {
        this(suppliers, 0);
    }

    public UniformCompositeSupplier(Iterable<Supplier<T>> suppliers, long seed) {
        super(seed);
        this.suppliers = Stream.ofAll(suppliers).toJavaList();
    }
    //endregion

    //region RandomDataSupplier Implementation
    @Override
    public T get() {
        return this.suppliers.get(this.random.nextInt(this.suppliers.size())).get();
    }
    //endregion

    //region Fields
    private List<Supplier<T>> suppliers;
    //endregion
}
