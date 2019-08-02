package com.yangdb.fuse.executor.ontology.schema;

/*-
 * #%L
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
 * #L%
 */

import com.yangdb.fuse.model.resourceInfo.FuseError;

import java.util.Collections;
import java.util.List;

public interface LoadResponse<S, F> {
    LoadResponse EMPTY = new LoadResponse() {
        @Override
        public List<CommitResponse> getResponses() {
            return Collections.emptyList();
        }

        @Override
        public LoadResponse response(CommitResponse response) {
            return this;
        }
    };

    List<CommitResponse<S, F>> getResponses();

    LoadResponse response(LoadResponse.CommitResponse<String, FuseError> response);

    interface CommitResponse<S, F> {
        CommitResponse EMPTY = new CommitResponse() {
            @Override
            public List getSuccesses() {
                return Collections.emptyList();
            }

            @Override
            public List getFailures() {
                return Collections.emptyList();
            }
        };

        List<S> getSuccesses();

        List<F> getFailures();

    }

}
