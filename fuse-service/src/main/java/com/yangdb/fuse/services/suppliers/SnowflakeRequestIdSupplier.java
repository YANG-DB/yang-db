package com.yangdb.fuse.services.suppliers;

/*-
 *
 * fuse-service
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

import com.google.inject.Inject;
import com.yangdb.fuse.dispatcher.driver.IdGeneratorDriver;
import com.yangdb.fuse.model.Range;
import com.twitter.snowflake.sequence.IdSequence;
import com.twitter.snowflake.support.IdSequenceFactory;

/**
 * Created by Roman on 4/9/2018.
 */
public class SnowflakeRequestIdSupplier implements RequestIdSupplier {
    private IdGeneratorDriver<Range> idGeneratorDriver;

    //region Constructors
    @Inject
    public SnowflakeRequestIdSupplier(IdGeneratorDriver<Range> idGeneratorDriver) {
        this.idGeneratorDriver = idGeneratorDriver;
    }

    private void init() {
        IdSequenceFactory idSequenceFactory = new IdSequenceFactory();
        idSequenceFactory.setTimeBits(41);
        idSequenceFactory.setWorkerBits(6);
        idSequenceFactory.setSeqBits(16);

        Range workerIdRange = idGeneratorDriver.getNext("workerId", 1);
        if(workerIdRange != null){
            workerId = workerIdRange.getLower() % (2 ^ 6);
            idSequenceFactory.setWorkerId(workerId);
        }else {
            idSequenceFactory.setWorkerId(1L);
        }
        this.sequence = idSequenceFactory.create();
    }
    //endregion

    //region RequestIdSupplier Implementation
    @Override
    public String get() {
        if(sequence==null) {
            init();
        }
        return String.format("FR%s", this.sequence.nextId());
    }

    public long getWorkerId() {
        return workerId;
    }

    //endregion

    //region Fields
    private long workerId;
    private IdSequence sequence;
    //endregion
}
