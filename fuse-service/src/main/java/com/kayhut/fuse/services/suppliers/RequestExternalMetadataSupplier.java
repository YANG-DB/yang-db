package com.kayhut.fuse.services.suppliers;

/*-
 * #%L
 * fuse-service
 * %%
 * Copyright (C) 2016 - 2018 yangdb   ------ www.yangdb.org ------
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
