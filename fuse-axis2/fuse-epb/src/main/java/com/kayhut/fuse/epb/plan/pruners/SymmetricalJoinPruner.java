package com.kayhut.fuse.epb.plan.pruners;

import com.kayhut.fuse.dispatcher.epb.PlanPruneStrategy;
import com.kayhut.fuse.dispatcher.utils.PlanUtil;
import com.kayhut.fuse.model.execution.plan.PlanOp;
import com.kayhut.fuse.model.execution.plan.PlanWithCost;
import com.kayhut.fuse.model.execution.plan.composite.Plan;
import com.kayhut.fuse.model.execution.plan.composite.descriptors.IterablePlanOpDescriptor;
import com.kayhut.fuse.model.execution.plan.costs.PlanDetailedCost;
import com.kayhut.fuse.model.execution.plan.entity.EntityJoinOp;

import java.util.*;

public class SymmetricalJoinPruner implements PlanPruneStrategy<PlanWithCost<Plan, PlanDetailedCost>> {
    @Override
    public Iterable<PlanWithCost<Plan, PlanDetailedCost>> prunePlans(Iterable<PlanWithCost<Plan, PlanDetailedCost>> plans) {
        SortedMap<String, Plan> plansMap = new TreeMap<>();
        for (PlanWithCost<Plan, PlanDetailedCost> plan : plans) {
            plansMap.put(IterablePlanOpDescriptor.getFull().describe(plan.getPlan().getOps()), plan.getPlan());
        }

        Set<String> planKeys = new HashSet<>();
        List<PlanWithCost<Plan, PlanDetailedCost>> keptPlans = new ArrayList<>();
        for (PlanWithCost<Plan, PlanDetailedCost> plan : plans) {
            if(plan.getPlan().getOps().size() == 1){
                Optional<EntityJoinOp> joinOp = PlanUtil.first(plan.getPlan(), EntityJoinOp.class);
                if(joinOp.isPresent()){
                    EntityJoinOp newJoin = new EntityJoinOp(joinOp.get().getRightBranch(), joinOp.get().getLeftBranch());
                    String desc = IterablePlanOpDescriptor.getFull().describe(Collections.singleton(newJoin));
                    if(planKeys.contains(desc)){
                       continue;
                    }
                }
            }
            String desc = IterablePlanOpDescriptor.getFull().describe(plan.getPlan().getOps());
            planKeys.add(desc);
            keptPlans.add(plan);
        }

        return keptPlans;
    }
}
