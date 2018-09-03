package com.fuse.domain.knowledge.datagen.dataSuppliers;

import java.util.Date;
import java.util.Random;
import java.util.function.Supplier;

/**
 * Created by Roman on 6/22/2018.
 */
public class UnifromBoundedDateSupplier extends RandomDataSupplier<Date> {
    //region Constructors
    public UnifromBoundedDateSupplier(long minEpoch, long maxEpoch) {
        this(minEpoch, maxEpoch, 0);
    }

    public UnifromBoundedDateSupplier(long minEpoch, long maxEpoch, long seed) {
        super(seed);
        this.minEpoch = minEpoch;
        this.maxEpoch = maxEpoch;

        this.diff = (int)(this.maxEpoch - this.minEpoch);
    }
    //endregion

    //region Supplier Implementation
    @Override
    public Date get() {
        return new Date(this.minEpoch + this.random.nextInt(this.diff));
    }
    //endregion

    //region Fields
    private long minEpoch;
    private long maxEpoch;

    private int diff;
    //endregion
}
