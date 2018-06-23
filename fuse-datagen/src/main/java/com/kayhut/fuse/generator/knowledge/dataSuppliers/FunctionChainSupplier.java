package com.kayhut.fuse.generator.knowledge.dataSuppliers;

import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Created by Roman on 6/23/2018.
 */
public class FunctionChainSupplier<TIn, TOut> implements Supplier<TOut> {
    //region Constructors
    public FunctionChainSupplier(Supplier<TIn> supplier, Function<TIn, TOut> function) {
        this.supplier = supplier;
        this.function = function;
    }
    //endregion

    //region Supplier Implementation
    @Override
    public TOut get() {
        return this.function.apply(this.supplier.get());
    }
    //endregion

    //region Fields
    private Supplier<TIn> supplier;
    private Function<TIn, TOut> function;
    //endregion
}
