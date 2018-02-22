package com.kayhut.fuse.epb.plan.estimation.pattern;

import com.kayhut.fuse.dispatcher.epb.CostEstimator;
import com.kayhut.fuse.epb.plan.estimation.IncrementalEstimationContext;
import com.kayhut.fuse.model.asgQuery.AsgQuery;
import com.kayhut.fuse.model.execution.plan.PlanWithCost;
import com.kayhut.fuse.model.execution.plan.composite.Plan;
import com.kayhut.fuse.model.execution.plan.costs.PlanDetailedCost;

/**
 * Created by lior.perry on 2/19/2018.
 */
public class FirstStepOnlyCostEstimator implements CostEstimator<Plan, PlanDetailedCost, IncrementalEstimationContext<Plan, PlanDetailedCost, AsgQuery>> {

    private CostEstimator<Plan, PlanDetailedCost, IncrementalEstimationContext<Plan, PlanDetailedCost, AsgQuery>> estimator;

    public FirstStepOnlyCostEstimator(CostEstimator<Plan, PlanDetailedCost, IncrementalEstimationContext<Plan, PlanDetailedCost, AsgQuery>> estimator) {
        this.estimator = estimator;
    }

    @Override
    public PlanWithCost<Plan, PlanDetailedCost> estimate(Plan plan, IncrementalEstimationContext<Plan, PlanDetailedCost, AsgQuery> context) {
        if(plan.getOps().size()>2)
            return new PlanWithCost<>(plan, context.getPreviousCost().get().getCost());
        return estimator.estimate(plan, context);
    }
}
