package com.kayhut.fuse.model.execution.plan;

import com.kayhut.fuse.model.asgQuery.AsgEBase;
import com.kayhut.fuse.model.execution.plan.entity.EntityOp;
import com.kayhut.fuse.model.query.entity.EEntityBase;

/**
 * Created by Roman on 30/04/2017.
 */
public class GoToEntityOp extends EntityOp {
    //region Constructors
    public GoToEntityOp() {

    }

    public GoToEntityOp(AsgEBase<EEntityBase> entity) {
        super(entity);
    }
    //endregion
}
