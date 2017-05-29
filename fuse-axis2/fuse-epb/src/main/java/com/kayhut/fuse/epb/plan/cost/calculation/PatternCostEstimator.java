package com.kayhut.fuse.epb.plan.cost.calculation;

import com.kayhut.fuse.epb.plan.cost.StatisticsCostEstimator;
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
public interface PatternCostEstimator {
    StepEstimator.StepEstimatorResult estimate(StatisticsProvider statisticsProvider, Map<StatisticsCostEstimator.StatisticsCostEstimatorNames, PlanOpBase> patternParts, Optional<PlanWithCost<Plan, PlanDetailedCost>> previousCost);
}
