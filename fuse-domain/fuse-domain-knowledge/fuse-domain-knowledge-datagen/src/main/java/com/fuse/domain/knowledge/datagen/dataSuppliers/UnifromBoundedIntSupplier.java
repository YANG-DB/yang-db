package com.fuse.domain.knowledge.datagen.dataSuppliers;

import java.util.Date;

/**
 * Created by Roman on 6/23/2018.
 */
public class UnifromBoundedIntSupplier extends RandomDataSupplier<Integer> {
    //region Constructors
    public UnifromBoundedIntSupplier(int min, int max) {
        this(min, max, 0);
    }

    public UnifromBoundedIntSupplier(int min, int max, long seed) {
        super(seed);
        this.min = min;
        this.max = max;

        this.diff = this.max - this.min;
    }
    //endregion

    //region Supplier Implementation
    @Override
    public Integer get() {
        return this.min + this.random.nextInt(this.diff);
    }
    //endregion

    //region Fields
    private int min;
    private int max;

    private int diff;
    //endregion
}
