package com.kayhut.fuse.epb.plan.pruners;

import com.kayhut.fuse.dispatcher.epb.PlanPruneStrategy;
import com.kayhut.fuse.model.execution.plan.PlanWithCost;
import com.kayhut.fuse.model.execution.plan.composite.Plan;
import com.kayhut.fuse.model.execution.plan.costs.PlanDetailedCost;
import javaslang.collection.Stream;

public class CheapestPlanPruneStrategy implements PlanPruneStrategy<PlanWithCost<Plan, PlanDetailedCost>> {

    @Override
    public Iterable<PlanWithCost<Plan, PlanDetailedCost>> prunePlans(Iterable<PlanWithCost<Plan, PlanDetailedCost>> plans) {
        return Stream.ofAll(plans).minBy((o1, o2) -> {
            if (Double.compare(o1.getCost().getGlobalCost().cost, o2.getCost().getGlobalCost().cost) == 0) {
                return Integer.compare(o1.getPlan().toString().hashCode(), o2.getPlan().toString().hashCode());
            }
            return Double.compare(o1.getCost().getGlobalCost().cost, o2.getCost().getGlobalCost().cost);

        }).toJavaList();
    }
}
