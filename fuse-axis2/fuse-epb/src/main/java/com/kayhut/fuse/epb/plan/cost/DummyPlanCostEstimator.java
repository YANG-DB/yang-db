package com.kayhut.fuse.epb.plan.cost;

import com.kayhut.fuse.model.execution.plan.Plan;
import com.kayhut.fuse.model.execution.plan.costs.CostCalculator;
import com.kayhut.fuse.model.execution.plan.costs.PlanCostEstimator;

/**
 * Created by moti on 3/29/2017.
 */
public class DummyPlanCostEstimator implements PlanCostEstimator<Plan<CostCalculator.Cost>,CostCalculator.Cost> {

    @Override
    public CostCalculator.Cost estimateCost(Plan<CostCalculator.Cost> newPlan) {
        return new CostCalculator.Cost(0.0,0,0);
    }
}
