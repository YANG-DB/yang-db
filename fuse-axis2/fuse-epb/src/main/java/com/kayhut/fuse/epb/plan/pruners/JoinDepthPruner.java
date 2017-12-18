package com.kayhut.fuse.epb.plan.pruners;

import com.kayhut.fuse.dispatcher.epb.PlanPruneStrategy;
import com.kayhut.fuse.model.execution.plan.PlanOp;
import com.kayhut.fuse.model.execution.plan.composite.Plan;
import com.kayhut.fuse.model.execution.plan.entity.EntityJoinOp;
import javaslang.collection.Stream;

public class JoinDepthPruner implements PlanPruneStrategy<Plan> {
    @Override
    public Iterable<Plan> prunePlans(Iterable<Plan> plans) {
        return Stream.ofAll(plans).filter(p -> isPlanValid(p, maxDepth)).toJavaList();
    }

    private boolean isPlanValid(Plan plan, int depth){
        if(depth == 0)
            return false;
        boolean valid = true;

        for (PlanOp planOp : plan.getOps()) {
            if(planOp instanceof EntityJoinOp){
                valid &= isPlanValid(((EntityJoinOp) planOp).getLeftBranch(), depth-1);
                valid &= isPlanValid(((EntityJoinOp) planOp).getRightBranch(), depth-1);
            }
        }
        return valid;
    }

    private int maxDepth = 3;
}
