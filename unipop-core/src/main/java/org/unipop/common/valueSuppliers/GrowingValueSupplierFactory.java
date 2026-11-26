package org.unipop.common.valueSuppliers;

/*-
 * #%L
 * unipop-core
 * %%
 * Copyright (C) 2016 - 2026 The YangDb Graph Database Project
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

/**
 * Bulk sizes that grow: {@code start}, then times {@code multiplier} for every bulk, up to {@code max}
 * (e.g. 1000, 2000, 4000, 8000, 10000, 10000, ...).
 * <p>
 * A step reads its input in bulks and sends one search per bulk. Small first bulks keep a small first page cheap
 * (it only needs the first bulk); large results quickly reach big bulks, so they need few searches. Measured
 * (docs/benchmark.md, Phase 7.4): a fixed bulk of 10,000 halves OpenSearch work for whole results but makes a
 * 100-row first page up to 5 times slower; growing from 1,000 keeps both.
 * <p>
 * Stateful: use one instance per step execution, so that every execution starts small again.
 */
public class GrowingValueSupplierFactory implements Supplier<Supplier<Integer>> {
    private final int max;
    private final int multiplier;
    private int next;

    public GrowingValueSupplierFactory(int start, int multiplier, int max) {
        if (start < 1 || multiplier < 1 || max < start) {
            throw new IllegalArgumentException(String.format(
                    "bulk sizes: need 1 <= start <= max and multiplier >= 1 (start=%d, multiplier=%d, max=%d)",
                    start, multiplier, max));
        }
        this.next = start;
        this.multiplier = multiplier;
        this.max = max;
    }

    @Override
    public synchronized Supplier<Integer> get() {
        int size = this.next;
        this.next = (int) Math.min((long) this.max, (long) this.next * this.multiplier);
        return () -> size;
    }
}
