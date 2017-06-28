package com.kayhut.fuse.epb.plan.estimation.step;

import com.kayhut.fuse.epb.plan.estimation.StatisticsCostEstimator;
import com.kayhut.fuse.epb.plan.statistics.StatisticsProvider;
import com.kayhut.fuse.model.execution.plan.Plan;
import com.kayhut.fuse.model.execution.plan.PlanOpBase;
import com.kayhut.fuse.model.execution.plan.PlanWithCost;
import com.kayhut.fuse.model.execution.plan.costs.CountEstimatesCost;
import com.kayhut.fuse.model.execution.plan.costs.PlanDetailedCost;

import java.util.Map;
import java.util.Optional;

import static com.kayhut.fuse.epb.plan.estimation.StatisticsCostEstimator.StatisticsCostEstimatorNames.GOTO_ENTITY;

/**
 * Created by moti on 29/05/2017.
 */
public class GoToStepPatternCostEstimator implements StepPatternCostEstimator {
    //region Constructors
    public GoToStepPatternCostEstimator(CostEstimationConfig config) {
        this.config = config;
    }
    //endregion

    //region StepPatternCostEstimator Implementation
    @Override
    public StepCostEstimator.StepEstimatorResult estimate(
            StatisticsProvider statisticsProvider,
            Map<StatisticsCostEstimator.StatisticsCostEstimatorNames, PlanOpBase> patternParts,
            Optional<PlanWithCost<Plan, PlanDetailedCost>> previousCost) {

        StepCostEstimator.StepEstimatorResult stepEstimatorResult = FullStepPatternCostEstimator.calculateFullStep(
                config,
                statisticsProvider,
                previousCost.get(),
                Step.buildGoToStep(previousCost.get().getPlan(), patternParts));

        CountEstimatesCost gotoCost = new CountEstimatesCost(0, 0);

        return StepCostEstimator.StepEstimatorResult.of(
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
