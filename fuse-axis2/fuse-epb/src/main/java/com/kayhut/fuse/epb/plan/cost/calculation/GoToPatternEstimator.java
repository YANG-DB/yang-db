package com.kayhut.fuse.epb.plan.cost.calculation;

import com.kayhut.fuse.epb.plan.cost.StatisticsCostEstimator;
import com.kayhut.fuse.epb.plan.statistics.StatisticsProvider;
import com.kayhut.fuse.model.execution.plan.Plan;
import com.kayhut.fuse.model.execution.plan.PlanOpBase;
import com.kayhut.fuse.model.execution.plan.PlanOpWithCost;
import com.kayhut.fuse.model.execution.plan.PlanWithCost;
import com.kayhut.fuse.model.execution.plan.costs.CountEstimatesCost;
import com.kayhut.fuse.model.execution.plan.costs.DoubleCost;
import com.kayhut.fuse.model.execution.plan.costs.PlanDetailedCost;

import java.util.Map;
import java.util.Optional;

import static com.kayhut.fuse.epb.plan.cost.StatisticsCostEstimator.StatisticsCostEstimatorNames.GOTO_ENTITY;

/**
 * Created by moti on 29/05/2017.
 */
public class GoToPatternEstimator implements PatternCostEstimator {
    //region Constructors
    public GoToPatternEstimator(CostEstimationConfig config) {
        this.config = config;
    }
    //endregion

    //region PatternCostEstimator Implementation
    @Override
    public StepEstimator.StepEstimatorResult estimate(
            StatisticsProvider statisticsProvider,
            Map<StatisticsCostEstimator.StatisticsCostEstimatorNames, PlanOpBase> patternParts,
            Optional<PlanWithCost<Plan, PlanDetailedCost>> previousCost) {

        StepEstimator.StepEstimatorResult stepEstimatorResult = FullStepPatternEstimator.calculateFullStep(
                config,
                statisticsProvider,
                previousCost.get(),
                Step.buildGoToStep(previousCost.get().getPlan(), patternParts));

        CountEstimatesCost gotoCost = new CountEstimatesCost(0, 0);

        return StepEstimator.StepEstimatorResult.of(
                stepEstimatorResult.lambda(),
                new PlanWithCost<>(new Plan(patternParts.get(GOTO_ENTITY)), gotoCost),
                stepEstimatorResult.getPlanStepCosts().get(1),
                stepEstimatorResult.getPlanStepCosts().get(2));
    }
    //endregion

    //region Fields
    private CostEstimationConfig config;
    //endregion
}
