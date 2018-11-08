package org.unipop.common.valueSuppliers;

import java.util.function.Supplier;

public class LinearDecayingValueSupplierFactory extends TimeBasedSupplierFactory {
    //region Constructors
    public LinearDecayingValueSupplierFactory(int maxValue, int minValue, long decayInterval) {
        this(maxValue, minValue, decayInterval, Clock.System.instance);
    }

    public LinearDecayingValueSupplierFactory(int maxValue, int minValue, long decayInterval, Clock clock) {
        super(clock);
        this.maxValue = maxValue;
        this.minValue = minValue;
        this.decayInterval = decayInterval;
        this.valueInterval = maxValue - minValue;
    }
    //endregion

    //region Supplier Implementation
    @Override
    public Supplier<Integer> get() {
        long startTime = this.clock.getTime();
        return new Impl(startTime);
    }
    //endregion

    //region
    private int maxValue;
    private int minValue;
    private int valueInterval;
    private long decayInterval;
    //endregion

    //region Impl
    private class Impl implements Supplier<Integer> {
        //region Constructors
        public Impl(long startTime) {
            this.startTime = startTime;
        }
        //endregion

        //region Supplier Implementation
        @Override
        public Integer get() {
            long elapsed = clock.getTime() - this.startTime;
            if (elapsed >= decayInterval) {
                return minValue;
            }

            return Math.round(maxValue - (((float) elapsed / decayInterval) * valueInterval));
        }
        //endregion

        //region Fields
        private long startTime;
        //endregion
    }
    //endregion
}
