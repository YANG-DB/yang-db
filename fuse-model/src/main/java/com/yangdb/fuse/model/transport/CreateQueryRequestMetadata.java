package com.yangdb.fuse.model.transport;

/*-
 * #%L
 * fuse-model
 * %%
 * Copyright (C) 2016 - 2019 The YangDb Graph Database Project
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
    String TYPE_V1_QUERY = "v1Query";
    String TYPE_CYPHER = "cypher";
    String TYPE_GRAPH_QL = "graphQL";
    String TYPE_GREMLIN = "gremlin";

    boolean isSearchPlan();

    String getId();

    String getName();

    String getOntology();

    T getQuery();

    StorageType getStorageType();

    /**
     * specific question format type:
     *  - V1 Query
     *  - Cypher Query
     *  - GraphQL Query
     * @return
     */
    String getType();

    /**
     * Type of physical query -
     *  - concrete - to be executed
     *  - parameterized - to be saved as parameterized template
     * @return
     */
    QueryType getQueryType();

    @JsonInclude(JsonInclude.Include.NON_NULL)
    CreateCursorRequest getCreateCursorRequest();

    long getTtl();

    PlanTraceOptions getPlanTraceOptions();

    enum StorageType {
        _stored,
        _volatile;
    }

    enum QueryType {
        concrete,
        parameterized


    }
}

