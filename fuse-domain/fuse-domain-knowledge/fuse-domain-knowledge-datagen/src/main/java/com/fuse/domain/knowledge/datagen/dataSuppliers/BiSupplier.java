package com.fuse.domain.knowledge.datagen.dataSuppliers;

import java.util.function.BiFunction;
import java.util.function.Supplier;

public class BiSupplier<TOut, TIn1, TIn2> implements Supplier<TOut> {
    //region Constructors
    public BiSupplier(Supplier<TIn1> supplier1, Supplier<TIn2> supplier2, BiFunction<TIn1, TIn2, TOut> supplierFunction) {
        this.supplier1 = supplier1;
        this.supplier2 = supplier2;
        this.supplierFunction = supplierFunction;
    }
    //endregion

    //region Supplier Implementation
    @Override
    public TOut get() {
        return this.supplierFunction.apply(this.supplier1.get(), this.supplier2.get());
    }
    //endregion

    //region Fields
    private Supplier<TIn1> supplier1;
    private Supplier<TIn2> supplier2;
    private BiFunction<TIn1, TIn2, TOut> supplierFunction;
    //endregion
}
