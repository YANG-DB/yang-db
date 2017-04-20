package com.kayhut.fuse.epb.plan.cost;

import com.kayhut.fuse.model.execution.plan.Plan;
import com.kayhut.fuse.model.execution.plan.PlanOpBase;
import com.kayhut.fuse.model.execution.plan.costs.Cost;
import com.kayhut.fuse.model.execution.plan.costs.CostEstimator;

/**
 * Created by moti on 3/28/2017.
 */
public class DummyCostEstimator implements CostEstimator<Cost> {

    @Override
    public Cost estimateCost(Plan<Cost> plan, PlanOpBase planOpBase) {
        return new Cost(0,0,0);
    }

    @Override
    public Cost estimateCost(PlanOpBase planOpBase) {
        return new Cost(0,0,0);
    }

    @Override
    public Cost estimateCost(Plan<Cost> plan) {
        return new Cost(0,0,0);
    }
}
