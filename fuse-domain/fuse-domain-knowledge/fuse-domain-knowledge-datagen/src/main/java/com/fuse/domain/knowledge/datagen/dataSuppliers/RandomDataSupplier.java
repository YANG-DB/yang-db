package com.fuse.domain.knowledge.datagen.dataSuppliers;

import java.util.Random;
import java.util.function.Supplier;

/**
 * Created by Roman on 6/22/2018.
 */
public abstract class RandomDataSupplier<T> implements Supplier<T> {
    //region Constructors
    public RandomDataSupplier() {
        this.random = new Random();
    }

    public RandomDataSupplier(long seed) {
        if (seed == 0) {
            this.random = new Random();
        }

        this.random = new Random(seed);
    }
    //endregion

    //region Fields
    protected Random random;
    //endregion
}
