package com.kayhut.fuse.model.execution.plan.composite;

import com.kayhut.fuse.model.execution.plan.FilterOp;
import com.kayhut.fuse.model.execution.plan.LogicalOperator;

import java.util.List;

/**
 * Created by User on 23/02/2017.
 */
public class CompositeFilterOp extends FilterOp {
    //region Constructors
    public CompositeFilterOp() {

    }

    public CompositeFilterOp(LogicalOperator op, List<FilterOp> filters) {
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

    public List<FilterOp> getFilters() {
        return this.filters;
    }

    public void setFilters(List<FilterOp> value) {
        this.filters = value;
    }
    //endregion

    //region Fields
    private LogicalOperator op;
    private List<FilterOp> filters;
    //endregion
}
