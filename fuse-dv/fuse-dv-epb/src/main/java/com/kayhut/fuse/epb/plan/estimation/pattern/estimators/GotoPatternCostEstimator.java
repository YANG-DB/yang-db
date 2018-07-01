package com.kayhut.fuse.epb.plan.estimation.pattern.estimators;

import com.kayhut.fuse.epb.plan.estimation.IncrementalEstimationContext;
import com.kayhut.fuse.epb.plan.estimation.pattern.GotoPattern;
import com.kayhut.fuse.epb.plan.estimation.pattern.Pattern;
import com.kayhut.fuse.model.asgQuery.AsgQuery;
import com.kayhut.fuse.model.execution.plan.PlanWithCost;
import com.kayhut.fuse.model.execution.plan.composite.Plan;
import com.kayhut.fuse.model.execution.plan.costs.CountEstimatesCost;
import com.kayhut.fuse.model.execution.plan.costs.PlanDetailedCost;

public class GotoPatternCostEstimator implements PatternCostEstimator<Plan, CountEstimatesCost, IncrementalEstimationContext<Plan, PlanDetailedCost, AsgQuery>> {
    @Override
    public Result<Plan, CountEstimatesCost> estimate(Pattern pattern, IncrementalEstimationContext<Plan, PlanDetailedCost, AsgQuery> context) {
        GotoPattern gotoPattern = (GotoPattern) pattern;

        PlanWithCost<Plan, CountEstimatesCost> entityOpCost = context.getPreviousCost().get().getCost().getPlanStepCost(gotoPattern.getEntityOp()).get();
        CountEstimatesCost gotoCost = new CountEstimatesCost(0, entityOpCost.getCost().peek());

        return Result.of(new double[]{1}, new PlanWithCost<>(new Plan(gotoPattern.getGoToEntityOp()), gotoCost));
    }
}
