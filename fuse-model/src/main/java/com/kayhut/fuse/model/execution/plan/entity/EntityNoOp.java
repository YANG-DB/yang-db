package com.kayhut.fuse.model.execution.plan.entity;

import com.kayhut.fuse.model.asgQuery.AsgEBase;
import com.kayhut.fuse.model.query.entity.EEntityBase;

/**
 * Created by roman.margolis on 27/11/2017.
 */
public class EntityNoOp extends EntityOp {
    //region Constructors
    public EntityNoOp() {
        super(new AsgEBase<>());
    }

    public EntityNoOp(AsgEBase<EEntityBase> entity) {
        super(entity);
    }
    //endregion
}
