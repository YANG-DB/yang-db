package com.yangdb.fuse.model.profile;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Query profiling step info
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class QueryProfileStepInfoData {
    private String stepName;
    private Long stepCount;
    private String stepQuery;

    public QueryProfileStepInfoData() {}

    public QueryProfileStepInfoData(String stepName, Long stepCount, String stepQuery) {
        this.stepName = stepName;
        this.stepCount = stepCount;
        this.stepQuery = stepQuery;
    }

    public void setStepName(String stepName) {
        this.stepName = stepName;
    }

    public void setStepCount(Long stepCount) {
        this.stepCount = stepCount;
    }

    public void setStepQuery(String stepQuery) {
        this.stepQuery = stepQuery;
    }

    public String getStepName() {
        return stepName;
    }

    public Long getStepCount() {
        return stepCount;
    }

    public String getStepQuery() {
        return stepQuery;
    }
}