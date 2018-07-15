package com.kayhut.fuse.assembly.knowledge.logical.model;


import com.fasterxml.jackson.annotation.JsonInclude;
import com.kayhut.fuse.model.results.Assignment;
import com.kayhut.fuse.model.results.QueryResultBase;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class LogicalModelQueryResult extends QueryResultBase {

    //region Constructors
    public LogicalModelQueryResult() {
        this.results = Collections.emptyList();
    }
    //endregion

    //region Properties

    //endregion

    //region Override Methods
    @Override
    public String toString() {
        return results.toString();
    }
    //endregion

    //region Fields
    private List<ElementBaseLogical> results;

    @Override
    public int getSize() {
        return this.results.size();
    }
    //endregion

    public static final class Builder {
        //region Constructors
        private LogicalModelQueryResult logicalModelQueryResult;

        private Builder() {
            results = new ArrayList<>();
        }
        //endregion

        //region Static
        public static Builder instance() {
            return new Builder();
        }
        //endregion

        //region Public Methods
        public Builder withResults(List<ElementBaseLogical> results) {
            this.results = results;
            return this;
        }

        public LogicalModelQueryResult build() {
            LogicalModelQueryResult logicalModelQueryResult = new LogicalModelQueryResult();
            return logicalModelQueryResult;
        }
        //endregion

        //region Fields
        private List<ElementBaseLogical> results;
        //endregion
    }


}
