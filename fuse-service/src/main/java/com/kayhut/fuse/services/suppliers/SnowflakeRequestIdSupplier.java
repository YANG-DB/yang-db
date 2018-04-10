package com.kayhut.fuse.services.suppliers;

import com.google.inject.Inject;
import com.twitter.snowflake.sequence.IdSequence;
import com.twitter.snowflake.support.IdSequenceFactory;

/**
 * Created by Roman on 4/9/2018.
 */
public class SnowflakeRequestIdSupplier implements RequestIdSupplier {
    //region Constructors
    @Inject
    public SnowflakeRequestIdSupplier() {
        IdSequenceFactory idSequenceFactory = new IdSequenceFactory();
        idSequenceFactory.setTimeBits(41);
        idSequenceFactory.setWorkerBits(6);
        idSequenceFactory.setSeqBits(16);

        idSequenceFactory.setWorkerId(1L);

        this.sequence = idSequenceFactory.create();
    }
    //endregion

    //region RequestIdSupplier Implementation
    @Override
    public String get() {
        return String.format("FR%s", this.sequence.nextId());
    }
    //endregion

    //region Fields
    private IdSequence sequence;
    //endregion
}
