package com.kayhut.fuse.epb.plan.cost;

import com.kayhut.fuse.model.execution.plan.Plan;
import com.kayhut.fuse.model.execution.plan.PlanOpBase;
import com.kayhut.fuse.model.execution.plan.costs.CostCalculator;
import com.kayhut.fuse.model.execution.plan.costs.CostEstimator;

/**
 * Created by moti on 3/28/2017.
 */
public class DummyCostEstimator implements CostEstimator<CostCalculator.Cost> {

    @Override
    public CostCalculator.Cost estimateCost(Plan<CostCalculator.Cost> plan, PlanOpBase planOpBase) {
        return new CostCalculator.Cost(0,0,0);
    }

    @Override
    public CostCalculator.Cost estimateCost(PlanOpBase planOpBase) {
        return new CostCalculator.Cost(0,0,0);
    }

    @Override
    public CostCalculator.Cost estimateCost(Plan<CostCalculator.Cost> plan) {
        return new CostCalculator.Cost(0,0,0);
    }
}
