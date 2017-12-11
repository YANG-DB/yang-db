package com.kayhut.fuse.model.execution.plan.entity;

import com.kayhut.fuse.model.execution.plan.PlanOp;
import com.kayhut.fuse.model.execution.plan.composite.Plan;

import java.util.Optional;

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

    public boolean isComplete(){
        Optional<PlanOp> entityOp = rightBranch.getOps().stream().filter(op -> EntityOp.class.isAssignableFrom(op.getClass())).reduce((a, b) -> b);
        if(entityOp.isPresent()){
            return ((EntityOp)entityOp.get()).getAsgEbase().geteNum() == this.getAsgEbase().geteNum();
        }
        return false;
    }

    private Plan leftBranch;
    private Plan rightBranch;
}
