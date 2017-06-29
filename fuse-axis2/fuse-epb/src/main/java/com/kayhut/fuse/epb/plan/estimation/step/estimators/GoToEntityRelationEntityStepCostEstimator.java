package com.kayhut.fuse.epb.plan.estimation.step.estimators;

import com.kayhut.fuse.epb.plan.estimation.CostEstimationConfig;
import com.kayhut.fuse.epb.plan.estimation.step.GoToEntityRelationEntityStep;
import com.kayhut.fuse.epb.plan.estimation.step.Step;
import com.kayhut.fuse.epb.plan.estimation.step.StepCostEstimator;
import com.kayhut.fuse.epb.plan.estimation.step.context.M1StepCostEstimatorContext;
import com.kayhut.fuse.model.execution.plan.Plan;
import com.kayhut.fuse.model.execution.plan.PlanWithCost;
import com.kayhut.fuse.model.execution.plan.costs.CountEstimatesCost;

/**
 * Created by moti on 29/05/2017.
 */
public class GoToEntityRelationEntityStepCostEstimator implements StepCostEstimator<Plan, CountEstimatesCost, M1StepCostEstimatorContext> {
    //region Constructors
    public GoToEntityRelationEntityStepCostEstimator(CostEstimationConfig config) {
        this.config = config;
    }
    //endregion

    //region StepPatternCostEstimator Implementation
    @Override
    public StepCostEstimator.Result<Plan, CountEstimatesCost> estimate(Step step, M1StepCostEstimatorContext context) {
        if (!step.getClass().equals(GoToEntityRelationEntityStep.class)) {
            return StepCostEstimator.EmptyResult.get();
        }

        GoToEntityRelationEntityStep goToEntityRelationEntityStep = (GoToEntityRelationEntityStep)step;

        StepCostEstimator.Result<Plan, CountEstimatesCost> stepEstimatorResult =
                EntityRelationEntityStepCostEstimator.calculateFullStep(
                        config,
                        context.getStatisticsProvider(),
                        context.getPreviousCost().get(),
                        goToEntityRelationEntityStep);

        CountEstimatesCost gotoCost = new CountEstimatesCost(0, 0);

        return StepCostEstimator.Result.of(
                stepEstimatorResult.lambda(),
                new PlanWithCost<>(new Plan(goToEntityRelationEntityStep.getStartGoTo()), gotoCost),
                stepEstimatorResult.getPlanStepCosts().get(1),
                stepEstimatorResult.getPlanStepCosts().get(2));
    }
    //endregion

    //region Fields
    private CostEstimationConfig config;
    //endregion
}
