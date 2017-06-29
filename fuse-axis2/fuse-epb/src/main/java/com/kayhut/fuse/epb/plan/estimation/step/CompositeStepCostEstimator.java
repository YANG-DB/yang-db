package com.kayhut.fuse.epb.plan.estimation.step;

import com.kayhut.fuse.epb.plan.estimation.step.context.M1StepPatternCostEstimatorContext;
import com.kayhut.fuse.epb.plan.estimation.step.pattern.StepPatternCostEstimator;
import com.kayhut.fuse.model.execution.plan.Plan;
import com.kayhut.fuse.model.execution.plan.PlanWithCost;
import com.kayhut.fuse.model.execution.plan.costs.CountEstimatesCost;
import com.kayhut.fuse.model.execution.plan.costs.PlanDetailedCost;

import java.util.Map;
import java.util.Optional;

/**
 * Created by moti on 29/05/2017.
 */
public class CompositeStepCostEstimator implements StepCostEstimator<Plan, CountEstimatesCost, M1StepPatternCostEstimatorContext> {
    //region Constructors
    public CompositeStepCostEstimator(
            Map<StatisticsCostEstimator.Pattern, StepPatternCostEstimator<Plan, CountEstimatesCost, M1StepPatternCostEstimatorContext>> patternEstimators) {
        this.patternEstimators = patternEstimators;
    }
    //endregion

    //region StepCostEstimator Implementation
    @Override
    public Result<Plan, CountEstimatesCost> estimate(Step step, M1StepPatternCostEstimatorContext context) {
        return patternEstimators.get(context.getPattern()).estimate(step, context);
    }
    //endregion

    //region Fields
    protected Map<StatisticsCostEstimator.Pattern,
            StepPatternCostEstimator<Plan, CountEstimatesCost, M1StepPatternCostEstimatorContext>> patternEstimators;
    //endregion
}
