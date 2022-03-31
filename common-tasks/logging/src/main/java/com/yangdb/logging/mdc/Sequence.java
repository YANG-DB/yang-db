package com.yangdb.logging.mdc;

/*-
 * #%L
 * logging
 * %%
 * Copyright (C) 2016 - 2022 The YangDb Graph Database Project
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

import com.yangdb.logging.slf4j.ExtendedMDC;
import org.slf4j.MDC;

import java.util.concurrent.atomic.AtomicInteger;

public class Sequence {
    //region Static
    public static int get() {
        AtomicInteger sequence = ExtendedMDC.get(Sequence.key);
        return sequence == null ? 0 : sequence.get();
    }

    public static MDCWriter of(int value) {
        return new InitialValue(value);
    }

    public static MDCWriter incr() {
        return new Incrementor(1);
    }
    //endregion

    public static class InitialValue extends MDCWriter.KeyValue<String> {
        //region Constructors
        public InitialValue(int value) {
            super(Sequence.key, Integer.toString(value));
            this.initialValue = value;
        }
        //endregion

        //region Override Methods
        @Override
        public void write() {
            AtomicInteger sequence = ExtendedMDC.get(Sequence.key);
            if (sequence == null) {
                ExtendedMDC.put(key, new AtomicInteger(this.initialValue));
            } else {
                sequence.set(this.initialValue);
            }

            super.write();
        }
        //endregion

        //region Fields
        private int initialValue;
        //endregion
    }

    public static class Incrementor implements MDCWriter {
        //region Constructors
        public Incrementor(int defaultValue) {
            this.defaultValue = defaultValue;
        }
        //endregion

        @Override
        public void write() {
            AtomicInteger sequence = ExtendedMDC.get(Sequence.key);
            if (sequence == null) {
                sequence = new AtomicInteger(this.defaultValue - 1);
            }

            int incrementedSequence = sequence.incrementAndGet();
            MDC.put(key, Integer.toString(incrementedSequence));
        }

        //region Fields
        protected int defaultValue;
        //endregion
    }

    public static final String key = "sequence";
}
