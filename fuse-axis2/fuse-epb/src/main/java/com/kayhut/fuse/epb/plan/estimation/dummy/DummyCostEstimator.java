package com.kayhut.fuse.epb.plan.estimation.dummy;

import com.kayhut.fuse.dispatcher.epb.CostEstimator;
import com.kayhut.fuse.model.execution.plan.PlanWithCost;

/**
 * Created by moti on 3/28/2017.
 */
public class DummyCostEstimator<P, C, TContext> implements CostEstimator<P, C, TContext> {
    public DummyCostEstimator(C dummyCost) {
        this.dummyCost = dummyCost;
    }


    private C dummyCost;

    @Override
    public PlanWithCost<P, C> estimate(P plan, TContext context) {
        return new PlanWithCost<>(plan, dummyCost);
    }
}
