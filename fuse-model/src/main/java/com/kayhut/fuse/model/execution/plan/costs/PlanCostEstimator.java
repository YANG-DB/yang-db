package com.kayhut.fuse.model.execution.plan.costs;

/**
 * Created by moti on 3/29/2017.
 */
public interface PlanCostEstimator<P ,C> {
    C estimateCost(P newPlan);
}
