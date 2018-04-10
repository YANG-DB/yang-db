package com.kayhut.fuse.services.suppliers;

import com.google.inject.Inject;

import java.util.function.Supplier;

public interface ExternalRequestIdSupplier extends Supplier<String> {
    class Impl implements ExternalRequestIdSupplier {
        //region Constructors
        @Inject
        public Impl(String externalRequestId) {
            this.externalRequestId = externalRequestId;
        }
        //endregion

        //region ExternalRequestIdSupplier Implementation
        @Override
        public String get() {
            return this.externalRequestId;
        }
        //endregion

        //region Fields
        private String externalRequestId;
        //endregion
    }
}
