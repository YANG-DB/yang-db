package com.kayhut.fuse.dispatcher.epb;

import com.kayhut.fuse.model.execution.plan.PlanWithCost;

import java.util.Optional;

/**
 * Created by moti on 3/27/2017.
 */
public interface CostEstimator<P, C, TContext> {
    PlanWithCost<P, C> estimate(P plan, TContext context);

}
