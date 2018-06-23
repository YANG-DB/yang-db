package com.kayhut.fuse.generator.knowledge.dataSuppliers;

import java.util.function.Supplier;

/**
 * Created by Roman on 6/23/2018.
 */
public class ProbabilisticCompositeSupplier<T> extends RandomDataSupplier<T> {
    //region Constructors
    public ProbabilisticCompositeSupplier(Supplier<T> supplier1, double pSupplier1, Supplier<T> supplier2) {
        this(supplier1, pSupplier1, supplier2, 0);
    }

    public ProbabilisticCompositeSupplier(Supplier<T> supplier1, double pSupplier1, Supplier<T> supplier2, long seed) {
        super(seed);
        this.supplier1 = supplier1;
        this.supplier2 = supplier2;
        this.pSupplier1 = pSupplier1;
    }
    //endregion

    //region Supplier Implementation
    @Override
    public T get() {
        return this.random.nextDouble() < pSupplier1 ?
                this.supplier1.get() :
                this.supplier2.get();
    }
    //endregion

    //region Fields
    private Supplier<T> supplier1;
    private double pSupplier1;
    private Supplier<T> supplier2;

    //endregion
}
