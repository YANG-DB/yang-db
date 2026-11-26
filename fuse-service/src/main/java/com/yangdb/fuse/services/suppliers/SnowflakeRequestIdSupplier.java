package com.yangdb.fuse.services.suppliers;

/*-
 * #%L
 * fuse-service
 * %%
 * Copyright (C) 2016 - 2019 The YangDb Graph Database Project
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

import com.google.inject.Inject;
import java.util.function.LongSupplier;
import com.yangdb.fuse.dispatcher.driver.IdGeneratorDriver;
import com.yangdb.fuse.model.Range;

/**
 * Created by Roman on 4/9/2018.
 */
/**
 * Request ids in the Snowflake layout: 41 bits of milliseconds since {@link #EPOCH}, 6 bits of worker id, 16 bits of
 * sequence. Unique and increasing per node.
 * <p>
 * When the system clock steps back (an NTP correction, a VM or laptop waking up), the ids keep counting from the last
 * timestamp instead of failing. The Snowflake library used before refused every id until the clock caught up, which
 * failed every request with HTTP 500 "Clock moved backwards" for that long (A11 in docs/revival/KNOWN_ISSUES.md).
 */
public class SnowflakeRequestIdSupplier implements RequestIdSupplier {
    /** 2020-01-01T00:00:00Z: 41 bits of milliseconds last until 2089 */
    static final long EPOCH = 1577836800000L;
    static final int WORKER_BITS = 6;
    static final int SEQUENCE_BITS = 16;
    private static final long MAX_SEQUENCE = (1L << SEQUENCE_BITS) - 1;

    private IdGeneratorDriver<Range> idGeneratorDriver;
    private final LongSupplier clock;

    //region Constructors
    @Inject
    public SnowflakeRequestIdSupplier(IdGeneratorDriver<Range> idGeneratorDriver) {
        this(idGeneratorDriver, System::currentTimeMillis);
    }

    SnowflakeRequestIdSupplier(IdGeneratorDriver<Range> idGeneratorDriver, LongSupplier clock) {
        this.idGeneratorDriver = idGeneratorDriver;
        this.clock = clock;
    }

    private void init() {
        Range workerIdRange = idGeneratorDriver.getNext("workerId", 1);
        //2^6 worker ids (the 6 worker bits); in Java ^ is XOR, so not (2 ^ 6)
        workerId = workerIdRange != null ? workerIdRange.getLower() % (1L << WORKER_BITS) : 1L;
        initialized = true;
    }
    //endregion

    //region RequestIdSupplier Implementation
    @Override
    public String get() {
        return String.format("FR%s", nextId());
    }

    synchronized long nextId() {
        if (!initialized) {
            init();
        }
        long now = clock.getAsLong();
        if (now > lastTimestamp) {
            lastTimestamp = now;
            sequence = 0;
        } else if (++sequence > MAX_SEQUENCE) {
            // the same millisecond (or a clock that stepped back) used up its sequence: borrow the next millisecond
            lastTimestamp++;
            sequence = 0;
        }
        return ((lastTimestamp - EPOCH) << (WORKER_BITS + SEQUENCE_BITS)) | (workerId << SEQUENCE_BITS) | sequence;
    }

    public long getWorkerId() {
        return workerId;
    }

    //endregion

    //region Fields
    private long workerId;
    private boolean initialized;
    private long lastTimestamp = -1;
    private long sequence;
    //endregion
}
