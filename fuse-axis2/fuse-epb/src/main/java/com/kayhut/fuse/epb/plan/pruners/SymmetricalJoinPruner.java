package com.kayhut.fuse.epb.plan.pruners;

import com.kayhut.fuse.dispatcher.epb.PlanPruneStrategy;
import com.kayhut.fuse.dispatcher.utils.PlanUtil;
import com.kayhut.fuse.model.execution.plan.PlanOp;
import com.kayhut.fuse.model.execution.plan.PlanWithCost;
import com.kayhut.fuse.model.execution.plan.composite.Plan;
import com.kayhut.fuse.model.execution.plan.composite.descriptors.IterablePlanOpDescriptor;
import com.kayhut.fuse.model.execution.plan.costs.PlanDetailedCost;
import com.kayhut.fuse.model.execution.plan.entity.EntityJoinOp;
import javaslang.collection.Stream;

import java.util.*;

/**
 * Removes symmetrical join plans. This pruner assumes that all plans which have a join as their
 */
public class SymmetricalJoinPruner implements PlanPruneStrategy<PlanWithCost<Plan, PlanDetailedCost>> {
    @Override
    public Iterable<PlanWithCost<Plan, PlanDetailedCost>> prunePlans(Iterable<PlanWithCost<Plan, PlanDetailedCost>> plans) {
     return Stream.ofAll(plans).filter(plan -> {
            if(plan.getPlan().getOps().size() == 1) {
                Optional<EntityJoinOp> joinOp = PlanUtil.first(plan.getPlan(), EntityJoinOp.class);
                if(joinOp.isPresent() && joinOp.get().isComplete()){
                    String leftDescription = IterablePlanOpDescriptor.getFull().describe(Collections.singleton(joinOp.get().getLeftBranch()));
                    String rightDescription = IterablePlanOpDescriptor.getFull().describe(Collections.singleton(joinOp.get().getRightBranch()));
                    return leftDescription.compareTo(rightDescription) < 0;
                }
            }
            return true;
        });
    }
}
