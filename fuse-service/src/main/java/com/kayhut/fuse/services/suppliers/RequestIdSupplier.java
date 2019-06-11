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
