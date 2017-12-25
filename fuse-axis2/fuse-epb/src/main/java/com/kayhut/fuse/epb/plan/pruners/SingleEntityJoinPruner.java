package com.kayhut.fuse.epb.plan.pruners;

import com.kayhut.fuse.dispatcher.epb.PlanPruneStrategy;
import com.kayhut.fuse.dispatcher.utils.PlanUtil;
import com.kayhut.fuse.model.execution.plan.PlanOp;
import com.kayhut.fuse.model.execution.plan.PlanWithCost;
import com.kayhut.fuse.model.execution.plan.composite.Plan;
import com.kayhut.fuse.model.execution.plan.costs.PlanDetailedCost;
import com.kayhut.fuse.model.execution.plan.entity.EntityJoinOp;
import com.kayhut.fuse.model.execution.plan.entity.EntityOp;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class SingleEntityJoinPruner implements PlanPruneStrategy<PlanWithCost<Plan, PlanDetailedCost>> {
    @Override
    public Iterable<PlanWithCost<Plan, PlanDetailedCost>> prunePlans(Iterable<PlanWithCost<Plan, PlanDetailedCost>> plans) {
        List<PlanWithCost<Plan, PlanDetailedCost>> selectedPlans = new ArrayList<>();
        for(PlanWithCost<Plan, PlanDetailedCost> plan : plans){
            if(!prunePlan(plan.getPlan()))
                selectedPlans.add(plan);
        }
        return selectedPlans;
    }

    private boolean prunePlan(Plan plan){
        boolean prune = false;
        for (PlanOp planOp : plan.getOps()) {
            if(planOp instanceof EntityJoinOp){
                prune |= !checkJoin((EntityJoinOp) planOp);
            }
        }

        return prune;
    }

    private boolean checkJoin(EntityJoinOp joinOp){
        boolean valid = true;
        if(joinOp.isComplete()){
            List<PlanOp> leftEntities = joinOp.getLeftBranch().getOps().stream().filter(op -> op instanceof EntityOp).collect(Collectors.toList());
            if(leftEntities.size() == 1 && leftEntities.get(0).getClass().equals(EntityOp.class))
                return false;
            List<PlanOp> rightEntities = joinOp.getRightBranch().getOps().stream().filter(op -> op instanceof EntityOp).collect(Collectors.toList());
            if(rightEntities.size() == 1 && rightEntities.get(0).getClass().equals(EntityOp.class))
                return false;
            valid &= joinOp.getLeftBranch().getOps().stream().filter(op -> op instanceof EntityJoinOp).allMatch(op -> checkJoin((EntityJoinOp) op));
            valid &= joinOp.getRightBranch().getOps().stream().filter(op -> op instanceof EntityJoinOp).allMatch(op -> checkJoin((EntityJoinOp) op));
        }

        return valid;
    }
}
