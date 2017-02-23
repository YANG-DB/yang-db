package com.kayhut.fuse.model.execution.plan;

import java.util.List;

/**
 * Created by User on 23/02/2017.
 */
public class CompositeFilterOp extends FilterOpBase {
    //region Constructors
    public CompositeFilterOp() {

    }

    public CompositeFilterOp(LogicalOperator op, List<FilterOpBase> filters) {
        this.op = op;
        this.filters = filters;
    }
    //endregion

    //region Properties
    public LogicalOperator getOp() {
        return this.op;
    }

    public void setOp(LogicalOperator op) {
        this.op = op;
    }

    public List<FilterOpBase> getFilters() {
        return this.filters;
    }

    public void setFilters(List<FilterOpBase> value) {
        this.filters = value;
    }
    //endregion

    //region Fields
    private LogicalOperator op;
    private List<FilterOpBase> filters;
    //endregion
}
