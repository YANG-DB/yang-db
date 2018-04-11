package com.kayhut.fuse.services.suppliers;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.kayhut.fuse.model.transport.ExternalMetadata;
import org.apache.commons.lang3.StringUtils;

import java.util.function.Supplier;

public interface RequestExternalMetadataSupplier extends Supplier<ExternalMetadata> {
    class Impl implements RequestExternalMetadataSupplier {
        //region Constructors
        @Inject
        public Impl(ExternalMetadata externalMetadata) {
            this.externalMetadata = externalMetadata;
        }
        //endregion

        //region ExternalRequestIdSupplier Implementation
        @Override
        public ExternalMetadata get() {
            return this.externalMetadata;
        }
        //endregion

        //region Fields
        private ExternalMetadata externalMetadata;
        //endregion
    }
}
