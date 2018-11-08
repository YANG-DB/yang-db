package org.unipop.common.valueSuppliers;

import java.util.function.Supplier;

public class FixedValueSupplierFactory extends TimeBasedSupplierFactory {
    //region Constructors
    public FixedValueSupplierFactory(int value) {
        super();
        this.value = value;
    }
    //endregion

    //region Supplier Implementation
    @Override
    public Supplier<Integer> get() {
        return () -> this.value;
    }
    //endregion

    //region Fields
    private int value;
    //endregion
}
