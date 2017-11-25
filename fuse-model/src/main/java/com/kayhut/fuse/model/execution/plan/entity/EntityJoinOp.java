package com.kayhut.fuse.model.execution.plan.entity;

import com.kayhut.fuse.model.execution.plan.composite.Plan;

/**
 * Created by mordechaic on 11/14/2017.
 */
public class EntityJoinOp extends EntityOp {
    public EntityJoinOp() {
    }

    public EntityJoinOp(Plan leftBranch, Plan rightBranch) {
        // Find last EntityOp in the left hand branch. A Join assumes both branches end at an entity.
        EntityOp entityOp = (EntityOp) leftBranch.getOps().stream().filter(op -> EntityOp.class.isAssignableFrom(op.getClass())).reduce((a, b) -> b).get();
        this.setAsgEbase(entityOp.getAsgEbase());
        this.leftBranch = leftBranch;
        this.rightBranch = rightBranch;
    }

    public Plan getLeftBranch() {
        return leftBranch;
    }

    public Plan getRightBranch() {
        return rightBranch;
    }

    private Plan leftBranch;
    private Plan rightBranch;
}
