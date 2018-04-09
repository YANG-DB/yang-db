package com.kayhut.fuse.services.suppliers;

import com.google.inject.Inject;

import java.util.function.Supplier;

public interface ExternalRequestIdSupplier extends Supplier<String> {
    class Impl implements ExternalRequestIdSupplier {
        //region Constructors
        @Inject
        public Impl(String externalId) {
            this.externalId = externalId;
        }
        //endregion

        //region ExternalRequestIdSupplier Implementation
        @Override
        public String get() {
            return this.externalId;
        }
        //endregion

        //region Fields
        private String externalId;
        //endregion
    }
}
