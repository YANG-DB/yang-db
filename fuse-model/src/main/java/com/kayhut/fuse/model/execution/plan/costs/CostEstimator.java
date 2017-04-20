package com.kayhut.fuse.model.execution.plan.costs;

import com.kayhut.fuse.model.execution.plan.Plan;
import com.kayhut.fuse.model.execution.plan.PlanOpBase;

/**
 * Created by moti on 3/27/2017.
 */
public interface CostEstimator<C> {
    C estimateCost(Plan<C> plan, PlanOpBase planOpBase);
    C estimateCost(PlanOpBase planOpBase);
    C estimateCost(Plan<C> plan);
}
