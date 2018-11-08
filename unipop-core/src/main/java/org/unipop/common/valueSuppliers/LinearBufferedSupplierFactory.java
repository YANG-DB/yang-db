package org.unipop.common.valueSuppliers;

import java.util.function.Supplier;

/**
 * Created by Roman on 8/21/2018.
 */
public class LinearBufferedSupplierFactory extends TimeBasedSupplierFactory {
    //region Constructors
    public LinearBufferedSupplierFactory(long[] timeBuffer, int[] valueBuffer) {
        this(timeBuffer, valueBuffer, Clock.System.instance);
    }

    public LinearBufferedSupplierFactory(long[] timeBuffer, int[] valueBuffer, Clock clock) {
        super(clock);
        this.timeBuffer = timeBuffer;
        this.valueBuffer = valueBuffer;
        this.lastValue = valueBuffer[valueBuffer.length - 1];
    }
    //endregion

    //region TimeBasedSupplierFactory Implementation
    @Override
    public Supplier<Integer> get() {
        return new Impl(this.clock.getTime());
    }
    //endregion

    //region Fields
    private long[] timeBuffer;
    private int[] valueBuffer;
    private int lastValue;
    //endregion

    private class Impl implements Supplier<Integer> {
        //region Constructors
        public Impl(long startTime) {
            this.startTime = startTime;
            this.bufferIndex = 0;
        }
        //endregion

        //region TimeBasedSupplierFactory Implementation
        @Override
        public Integer get() {
            long elapsed = clock.getTime() - this.startTime;

            while(this.bufferIndex < timeBuffer.length && elapsed >= timeBuffer[this.bufferIndex]) {
                this.bufferIndex += 1;
            }

            return this.bufferIndex < valueBuffer.length ?
                    valueBuffer[this.bufferIndex] :
                    lastValue;
        }
        //endregion

        //region Fields
        private long startTime;
        private int bufferIndex;
        //endregion
    }
}
