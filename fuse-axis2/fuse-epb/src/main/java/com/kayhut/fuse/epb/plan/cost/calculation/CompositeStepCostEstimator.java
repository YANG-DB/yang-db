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
public class CompositeStepCostEstimator implements StepEstimator {
    Map<StatisticsCostEstimator.StatisticsCostEstimatorPatterns, PatternCostEstimator> patternEstimators;

    public CompositeStepCostEstimator(Map<StatisticsCostEstimator.StatisticsCostEstimatorPatterns, PatternCostEstimator> patternEstimators) {
        this.patternEstimators = patternEstimators;
    }

    @Override
    public StepEstimatorResult calculate(StatisticsProvider statisticsProvider, Map<StatisticsCostEstimator.StatisticsCostEstimatorNames, PlanOpBase> patternParts, StatisticsCostEstimator.StatisticsCostEstimatorPatterns pattern, Optional<PlanWithCost<Plan, PlanDetailedCost>> previousCost) {
        return patternEstimators.get(pattern).estimate(statisticsProvider, patternParts, previousCost);
    }

}
