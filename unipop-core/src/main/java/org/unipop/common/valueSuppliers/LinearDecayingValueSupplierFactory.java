package org.unipop.common.valueSuppliers;

/*-
 * #%L
 * LinearDecayingValueSupplierFactory.java - unipop-core - kayhut - 2,016
 * org.codehaus.mojo-license-maven-plugin-1.16
 * $Id$
 * $HeadURL$
 * %%
 * Copyright (C) 2016 - 2018 yangdb   ------ www.yangdb.org ------
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
 * #L%
 */

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
