package com.kayhut.fuse.epb.plan.cost;

import com.kayhut.fuse.model.execution.plan.Plan;
import com.kayhut.fuse.model.execution.plan.PlanOpBase;
import com.kayhut.fuse.model.execution.plan.costs.SingleCost;

import java.util.Optional;

/**
 * Created by moti on 3/28/2017.
 */
public class DummyPlanOpCostEstimator implements PlanOpCostEstimator<SingleCost> {

    @Override
    public SingleCost estimateCost(Optional<Plan<SingleCost>> singleCostPlan, PlanOpBase planOpBase) {
        return new SingleCost(0);
    }
}
