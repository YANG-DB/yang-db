package com.kayhut.fuse.services.suppliers;

import com.google.inject.Inject;
import com.twitter.snowflake.sequence.IdSequence;
import com.twitter.snowflake.support.IdSequenceFactory;

import java.util.function.Supplier;

/**
 * Created by roman.margolis on 07/01/2018.
 */
public interface RequestIdSupplier extends Supplier<String> {
    class Impl implements RequestIdSupplier {
        //region Constructors
        @Inject
        public Impl() {
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
}
