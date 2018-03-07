package com.kayhut.fuse.model.execution.plan.relation;

import com.kayhut.fuse.model.asgQuery.AsgEBase;
import com.kayhut.fuse.model.execution.plan.AsgEBasePlanOp;
import com.kayhut.fuse.model.query.aggregation.AggBase;

/**
 * Created by User on 22/02/2017.
 */
public class RelationGroupingOp extends AsgEBasePlanOp<AggBase> {
    //region Constructors
    public RelationGroupingOp() {
        super(new AsgEBase<>());
    }

    public RelationGroupingOp(AsgEBase<AggBase> agg) {
        super(agg);
    }
    //endregion
}
