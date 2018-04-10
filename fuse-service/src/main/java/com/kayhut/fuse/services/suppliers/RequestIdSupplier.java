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
        public Impl(String requestId) {
            this.requestId = requestId;
        }
        //endregion

        //region RequestIdSupplier Implementation
        @Override
        public String get() {
            return this.requestId;
        }
        //endregion

        //region Fields
        private String requestId;
        //endregion
    }
}
