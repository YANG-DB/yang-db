package com.yangdb.fuse.executor.ontology.schema.load;

/*-
 *
 * fuse-dv-core
 * %%
 * Copyright (C) 2016 - 2019 The Fuse Graph Database Project
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
 *
 */

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.yangdb.fuse.model.resourceInfo.FuseError;

import java.util.ArrayList;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Response implements LoadResponse.CommitResponse<String, FuseError> {

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private List<FuseError> failures;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private List<String> success;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String index;

    public Response() {
    }

    public Response(String index) {
        this.index = index;
        this.failures = new ArrayList<>();
        this.success = new ArrayList<>();
    }

    public Response failure(FuseError err) {
        failures.add(err);
        return this;
    }

    public Response failure(List<FuseError> failed) {
        this.failures.addAll(failed);
        return this;
    }

    public Response success(String itemId) {
        success.add(itemId);
        return this;
    }

    public Response success(List<String> itemIds) {
        success.addAll(itemIds);
        return this;
    }

    public List<FuseError> getFailures() {
        return failures;
    }

    public List<String> getSuccesses() {
        return success;
    }

    public String getIndex() {
        return index;
    }

}
