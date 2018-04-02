package com.kayhut.fuse.model.execution.plan.entity;

import com.kayhut.fuse.model.asgQuery.AsgEBase;
import com.kayhut.fuse.model.execution.plan.entity.EntityOp;
import com.kayhut.fuse.model.query.entity.EEntityBase;

/**
 * Created by Roman on 30/04/2017.
 */
public class GoToEntityOp extends EntityOp {
    //region Constructors
    public GoToEntityOp() {
        super(new AsgEBase<>());
    }

    public GoToEntityOp(AsgEBase<EEntityBase> entity) {
        super(entity);
    }
    //endregion
}
