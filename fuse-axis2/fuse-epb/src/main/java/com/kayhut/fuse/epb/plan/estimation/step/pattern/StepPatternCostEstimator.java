package com.kayhut.fuse.epb.plan.estimation.step.pattern;

import com.kayhut.fuse.epb.plan.estimation.step.StatisticsCostEstimator;
import com.kayhut.fuse.epb.plan.estimation.step.Step;
import com.kayhut.fuse.epb.plan.estimation.step.StepCostEstimator;
import com.kayhut.fuse.epb.plan.statistics.StatisticsProvider;
import com.kayhut.fuse.model.execution.plan.Plan;
import com.kayhut.fuse.model.execution.plan.PlanOpBase;
import com.kayhut.fuse.model.execution.plan.PlanWithCost;
import com.kayhut.fuse.model.execution.plan.costs.PlanDetailedCost;

import java.util.Map;
import java.util.Optional;

/**
 * Created by moti on 29/05/2017.
 */
public interface StepPatternCostEstimator<P, C, TContext> {
    StepCostEstimator.Result<P, C> estimate(Step step, TContext context);
}
