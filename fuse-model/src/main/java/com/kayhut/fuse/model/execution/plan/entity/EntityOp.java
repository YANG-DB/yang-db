package com.kayhut.fuse.model.execution.plan.entity;

import com.kayhut.fuse.model.asgQuery.AsgEBase;
import com.kayhut.fuse.model.execution.plan.AsgEBasePlanOp;
import com.kayhut.fuse.model.query.entity.EEntityBase;

/**
 * Created by User on 20/02/2017.
 */
public class EntityOp extends AsgEBasePlanOp<EEntityBase> {
    //region Constructor
    public EntityOp() {}

    public EntityOp(AsgEBase<EEntityBase> asgEBase) {
        super(asgEBase);
    }
    //endregion
}
