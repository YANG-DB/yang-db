package com.kayhut.fuse.services.suppliers;

import com.google.inject.Inject;
import com.kayhut.fuse.dispatcher.driver.IdGeneratorDriver;
import com.kayhut.fuse.model.Range;
import com.kayhut.fuse.model.transport.ContentResponse;
import com.kayhut.fuse.services.controllers.IdGeneratorController;
import com.twitter.snowflake.sequence.IdSequence;
import com.twitter.snowflake.support.IdSequenceFactory;

/**
 * Created by Roman on 4/9/2018.
 */
public class SnowflakeRequestIdSupplier implements RequestIdSupplier {
    //region Constructors
    @Inject
    public SnowflakeRequestIdSupplier(IdGeneratorDriver<Range> idGeneratorDriver) {
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
