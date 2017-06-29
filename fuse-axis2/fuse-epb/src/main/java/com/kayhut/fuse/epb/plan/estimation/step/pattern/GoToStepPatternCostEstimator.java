package com.kayhut.fuse.epb.plan.estimation.step.pattern;

import com.kayhut.fuse.epb.plan.estimation.step.StatisticsCostEstimator;
import com.kayhut.fuse.epb.plan.estimation.CostEstimationConfig;
import com.kayhut.fuse.epb.plan.estimation.step.Step;
import com.kayhut.fuse.epb.plan.estimation.step.StepCostEstimator;
import com.kayhut.fuse.epb.plan.estimation.step.context.StatisticsPatternContext;
import com.kayhut.fuse.epb.plan.statistics.StatisticsProvider;
import com.kayhut.fuse.model.execution.plan.Plan;
import com.kayhut.fuse.model.execution.plan.PlanOpBase;
import com.kayhut.fuse.model.execution.plan.PlanWithCost;
import com.kayhut.fuse.model.execution.plan.costs.CountEstimatesCost;
import com.kayhut.fuse.model.execution.plan.costs.PlanDetailedCost;

import java.util.Map;
import java.util.Optional;

import static com.kayhut.fuse.epb.plan.estimation.step.StatisticsCostEstimator.PatternPart.GOTO_ENTITY;

/**
 * Created by moti on 29/05/2017.
 */
public class GoToStepPatternCostEstimator implements StepPatternCostEstimator<Plan, PlanDetailedCost, CountEstimatesCost, StatisticsPatternContext> {
    //region Constructors
    public GoToStepPatternCostEstimator(CostEstimationConfig config) {
        this.config = config;
    }
    //endregion

    //region StepPatternCostEstimator Implementation
    @Override
    public StepCostEstimator.Result<Plan, CountEstimatesCost> estimate(
            Optional<PlanWithCost<Plan, PlanDetailedCost>> previousCost,
            StatisticsPatternContext context) {

        StepCostEstimator.Result<Plan, CountEstimatesCost> stepEstimatorResult = FullStepPatternCostEstimator.calculateFullStep(
                config,
                context.getStatisticsProvider(),
                previousCost.get(),
                Step.buildGoToStep(previousCost.get().getPlan(), context.getPatternParts()));

        CountEstimatesCost gotoCost = new CountEstimatesCost(0, 0);

        return StepCostEstimator.Result.of(
                stepEstimatorResult.lambda(),
                new PlanWithCost<>(new Plan(context.getPatternParts().get(GOTO_ENTITY)), gotoCost),
                stepEstimatorResult.getPlanStepCosts().get(1),
                stepEstimatorResult.getPlanStepCosts().get(2));
    }
    //endregion

    //region Fields
    private CostEstimationConfig config;
    //endregion
}
