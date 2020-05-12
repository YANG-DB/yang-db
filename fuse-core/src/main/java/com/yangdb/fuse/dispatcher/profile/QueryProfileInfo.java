package com.yangdb.fuse.dispatcher.profile;

/*-
 * #%L
 * fuse-core
 * %%
 * Copyright (C) 2016 - 2020 The YangDb Graph Database Project
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

import com.yangdb.fuse.model.profile.QueryProfileStepInfoData;
import org.apache.tinkerpop.gremlin.process.traversal.util.Metrics;

import java.util.List;
import java.util.stream.Collectors;

/**
 * profiling info for
 */
public interface QueryProfileInfo {

    Metrics measurements();

    List<QueryProfileStepInfoData> infoData();

    class QueryProfileInfoImpl implements QueryProfileInfo {
        private Metrics measurements;

        public QueryProfileInfoImpl(Metrics measurements) {
            this.measurements = measurements;
        }

        @Override
        public Metrics measurements() {
            return measurements;
        }

        public List<QueryProfileStepInfoData> infoData() {
            return this.measurements().getCounts().entrySet().stream()
                    .map(e->new QueryProfileStepInfoData(e.getKey(),e.getValue(),
                            this.measurements().getAnnotation(e.getKey()).toString())).collect(Collectors.toList());
        }
    }


}
