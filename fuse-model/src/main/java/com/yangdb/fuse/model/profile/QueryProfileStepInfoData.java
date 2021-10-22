package com.yangdb.fuse.model.profile;

/*-
 * #%L
 * fuse-model
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

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Query profiling step info
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class QueryProfileStepInfoData {
    private String stepName;
    private long stepCount;
    private long batchSize;
    private String stepQuery;

    public QueryProfileStepInfoData() {}

    public QueryProfileStepInfoData(String stepName, long stepCount, String stepQuery) {
        this.stepName = stepName;
        this.stepCount = stepCount;
        this.stepQuery = stepQuery;
    }

    public QueryProfileStepInfoData(String stepName, long stepCount,long batchSize, String stepQuery) {
        this.stepName = stepName;
        this.stepCount = stepCount;
        this.batchSize = batchSize;
        this.stepQuery = stepQuery;
    }

    public void setStepName(String stepName) {
        this.stepName = stepName;
    }

    public void setStepQuery(String stepQuery) {
        this.stepQuery = stepQuery;
    }

    public String getStepName() {
        return stepName;
    }

    public String getStepQuery() {
        return stepQuery;
    }

    public void setStepCount(long stepCount) {
        this.stepCount = stepCount;
    }

    public long getStepCount() {
        return stepCount;
    }

    public long getBatchSize() {
        return batchSize;
    }

    public void setBatchSize(long batchSize) {
        this.batchSize = batchSize;
    }
}
