package com.kayhut.fuse.epb.plan.cost;

import com.kayhut.fuse.model.execution.plan.Plan;

/**
 * Created by moti on 3/29/2017.
 */
public interface PlanCostEstimator<C> {
    C estimateCost(Plan<C> newPlan);
}
