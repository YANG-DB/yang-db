package com.kayhut.fuse.epb.plan.cost;

import com.kayhut.fuse.model.execution.plan.PlanWithCost;

import java.util.Optional;

/**
 * Created by moti on 3/28/2017.
 */
public class DummyCostEstimator<P, C, Q> implements CostEstimator<P, C, Q> {
    public DummyCostEstimator(C dummyCost) {
        this.dummyCost = dummyCost;
    }


    private C dummyCost;

    @Override
    public PlanWithCost<P, C> estimate(P plan, Optional<PlanWithCost<P, C>> previousCost, Q query) {
        return new PlanWithCost<>(plan, previousCost.map(PlanWithCost::getCost).orElse(dummyCost));
    }
}
