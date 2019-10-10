package org.unipop.common.valueSuppliers;

/*-
 *
 * LinearBufferedSupplierFactory.java - unipop-core - yangdb - 2,016
 * org.codehaus.mojo-license-maven-plugin-1.16
 * $Id$
 * $HeadURL$
 * %%
 * Copyright (C) 2016 - 2019 yangdb   ------ www.yangdb.org ------
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

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
