package com.yangdb.fuse.model.transport;

/*-
 * #%L
 * fuse-model
 * %%
 * Copyright (C) 2016 - 2018 The Fuse Graph Database Project
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

import com.fasterxml.jackson.annotation.JsonInclude;
import com.yangdb.fuse.model.transport.cursor.CreateCursorRequest;

public interface CreateQueryRequestMetadata<T> {
    boolean isSearchPlan();

    String getId();

    String getName();

    String getOntology();

    T getQuery();

    StorageType getStorageType();

    @JsonInclude(JsonInclude.Include.NON_NULL)
    CreateCursorRequest getCreateCursorRequest();

    long getTtl();

    PlanTraceOptions getPlanTraceOptions();

        enum StorageType {
        _stored,
        _volatile;
    }
}
