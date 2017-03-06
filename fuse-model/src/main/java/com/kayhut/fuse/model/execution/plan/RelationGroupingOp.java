package com.kayhut.fuse.model.execution.plan;

import com.kayhut.fuse.model.asgQuery.AsgEBase;
import com.kayhut.fuse.model.query.aggregation.AggBase;

/**
 * Created by User on 22/02/2017.
 */
public class RelationGroupingOp extends PlanOpBase {
    //region Constructors
    public RelationGroupingOp() {

    }

    public RelationGroupingOp(AsgEBase<AggBase> agg) {
        this.agg = agg;
    }
    //endregion

    //region Properties

    public AsgEBase<AggBase> getAgg() {
        return agg;
    }

    public void setAgg(AsgEBase<AggBase> agg) {
        this.agg = agg;
    }

    //endregion

    //region Methods

    @Override
    public int geteNum() {
        return this.agg.geteNum();
    }

    //endregion

    //region Fields
    private AsgEBase<AggBase> agg;
    //endregion
}
