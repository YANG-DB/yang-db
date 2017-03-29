package com.kayhut.fuse.epb.plan.cost;

import com.kayhut.fuse.model.execution.plan.Plan;
import com.kayhut.fuse.model.execution.plan.costs.SingleCost;

/**
 * Created by moti on 3/29/2017.
 */
public class DummyPlanCostEstimator implements PlanCostEstimator<SingleCost> {

    @Override
    public SingleCost estimateCost(Plan<SingleCost> newPlan) {
        return new SingleCost(0.0);
    }
}
