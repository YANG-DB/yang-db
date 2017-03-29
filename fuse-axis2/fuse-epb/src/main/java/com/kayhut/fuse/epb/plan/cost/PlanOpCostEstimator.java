package com.kayhut.fuse.epb.plan.cost;

import com.kayhut.fuse.model.execution.plan.Plan;
import com.kayhut.fuse.model.execution.plan.PlanOpBase;

import java.util.Optional;

/**
 * Created by moti on 3/27/2017.
 */
public interface PlanOpCostEstimator<C> {
    C estimateCost(Optional<Plan<C>> plan, PlanOpBase planOpBase);
}
