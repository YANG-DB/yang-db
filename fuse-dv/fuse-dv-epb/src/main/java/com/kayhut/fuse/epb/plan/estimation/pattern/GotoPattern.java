package com.kayhut.fuse.epb.plan.estimation.pattern;

import com.kayhut.fuse.model.execution.plan.entity.EntityOp;
import com.kayhut.fuse.model.execution.plan.entity.GoToEntityOp;

public class GotoPattern extends Pattern {

    public GotoPattern(GoToEntityOp goToEntityOp, EntityOp entityOp) {
        this.goToEntityOp = goToEntityOp;
        this.entityOp = entityOp;
    }

    public GoToEntityOp getGoToEntityOp() {
        return goToEntityOp;
    }

    public EntityOp getEntityOp() {
        return entityOp;
    }

    private GoToEntityOp goToEntityOp;
    private EntityOp entityOp;

}
