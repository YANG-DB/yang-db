package com.yangdb.fuse.executor.ontology.schema.load;

/*-
 * #%L
 * fuse-dv-core
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

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.yangdb.fuse.model.resourceInfo.FuseError;

import java.util.ArrayList;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class LoadResponseImpl implements LoadResponse<String, FuseError> {


    @JsonInclude(JsonInclude.Include.NON_NULL)
    private List<CommitResponse<String, FuseError>> responses;

    public LoadResponseImpl() {
        this.responses = new ArrayList<>();
    }

    public LoadResponse response(CommitResponse<String, FuseError> response) {
        this.responses.add(response);
        return this;
    }

    @Override
    public List<CommitResponse<String, FuseError>> getResponses() {
        return responses;
    }
}
