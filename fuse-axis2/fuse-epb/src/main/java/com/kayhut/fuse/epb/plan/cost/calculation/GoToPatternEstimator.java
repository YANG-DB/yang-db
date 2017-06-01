package com.kayhut.fuse.epb.plan.cost.calculation;

import com.kayhut.fuse.epb.plan.cost.StatisticsCostEstimator;
import com.kayhut.fuse.epb.plan.statistics.StatisticsProvider;
import com.kayhut.fuse.model.execution.plan.Plan;
import com.kayhut.fuse.model.execution.plan.PlanOpBase;
import com.kayhut.fuse.model.execution.plan.PlanOpWithCost;
import com.kayhut.fuse.model.execution.plan.PlanWithCost;
import com.kayhut.fuse.model.execution.plan.costs.Cost;
import com.kayhut.fuse.model.execution.plan.costs.PlanDetailedCost;

import java.util.Map;
import java.util.Optional;

import static com.kayhut.fuse.epb.plan.cost.StatisticsCostEstimator.StatisticsCostEstimatorNames.GOTO_ENTITY;

/**
 * Created by moti on 29/05/2017.
 */
public class GoToPatternEstimator implements PatternCostEstimator {
    private CostEstimationConfig config;

    public GoToPatternEstimator(CostEstimationConfig config) {
        this.config = config;
    }

    @Override
    public StepEstimator.StepEstimatorResult estimate(StatisticsProvider statisticsProvider, Map<StatisticsCostEstimator.StatisticsCostEstimatorNames, PlanOpBase> patternParts, Optional<PlanWithCost<Plan, PlanDetailedCost>> previousCost) {
        StepEstimator.StepEstimatorResult stepEstimatorResult = FullStepPatternEstimator.calculateFullStep(config,statisticsProvider,  previousCost.get(), Step.buildGoToStep(previousCost.get().getPlan(), patternParts));
        Cost gotoCost = new Cost(0);

        return StepEstimator.StepEstimatorResult.of(stepEstimatorResult.lambda(), new PlanOpWithCost<>(gotoCost, 0, patternParts.get(GOTO_ENTITY)), stepEstimatorResult.planOpWithCosts().get(1), stepEstimatorResult.planOpWithCosts().get(2));

    }
}
