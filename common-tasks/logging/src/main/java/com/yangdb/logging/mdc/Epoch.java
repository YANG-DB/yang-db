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

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

public class Epoch {
    public static class StartNanos {
        public static long get() {
            String startNanos = MDC.get(key);
            try {
                return startNanos == null ? 0 : Long.parseLong(startNanos);
            } catch (NumberFormatException ex) {
                return 0;
            }
        }

        public static MDCWriter of(long startNanos) {
            return new MDCWriter.KeyValue(key, Long.toString(startNanos));
        }

        public static MDCWriter now() {
            return new MDCWriter.KeyValue(key, Long.toString(System.nanoTime()));
        }

        public static final String key = "startNanos";
    }

    public static class FromNanos {
        public static MDCWriter of(long fromNanos) {
            return new FromNanosNext(fromNanos);
        }

        public static MDCWriter now() {
            return new FromNanosNext(System.nanoTime());
        }

        public static class FromNanosNext implements MDCWriter {
            //region Static
            public static final String key = "fromNanosNext";
            //endegion

            //region Constructors
            public FromNanosNext(long fromNanos) {
                this.fromNanos = fromNanos;
            }
            //endregion

            //region MDCWriter Implementation
            @Override
            public void write() {
                AtomicLong fromNanosNext = ExtendedMDC.get(key);
                if (fromNanosNext == null) {
                    ExtendedMDC.put(key, new AtomicLong(this.fromNanos));
                } else {
                    MDC.put(FromNanos.key, Long.toString(fromNanosNext.getAndSet(this.fromNanos)));
                }
            }
            //endregion

            //region Fields
            private long fromNanos;
            //endregion
        }

        public static final String key = "fromNanos";
    }

    public static class Elapsed {
        public static long get(TimeUnit timeUnit) {
            long startNanos = Epoch.StartNanos.get();
            return startNanos == 0 ? 0 : timeUnit.convert(System.nanoTime() - startNanos, TimeUnit.NANOSECONDS);
        }
    }
}
