package com.kayhut.fuse.model.execution.plan;

import com.kayhut.fuse.model.query.aggregation.AggBase;

/**
 * Created by User on 22/02/2017.
 */
public class RelationGroupingOp extends PlanOpBase {
    //region Constructors
    public RelationGroupingOp() {

    }

    public RelationGroupingOp(AggBase agg) {
        this.agg = agg;
    }
    //endregion

    //region Properties
    public AggBase getAgg() {
        return this.agg;
    }

    public void setAgg(AggBase value) {
        this.agg = value;
    }
    //endregion

    //region Fields
    private AggBase agg;
    //endregion
}
