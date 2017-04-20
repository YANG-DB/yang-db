package com.kayhut.fuse.epb.plan.cost;

import com.kayhut.fuse.model.execution.plan.Plan;
import com.kayhut.fuse.model.execution.plan.PlanOpBase;
import com.kayhut.fuse.model.execution.plan.PlanWithCost;

import java.util.Optional;

/**
 * Created by moti on 3/27/2017.
 */
public interface CostEstimator<P, C> {
    PlanWithCost<P, C> estimate(P plan, Optional<C> previousCost);
}
