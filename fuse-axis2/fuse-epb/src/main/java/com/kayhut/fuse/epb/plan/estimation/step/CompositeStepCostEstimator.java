package com.kayhut.fuse.epb.plan.estimation.step;

import com.kayhut.fuse.epb.plan.estimation.step.context.StatisticsPatternContext;
import com.kayhut.fuse.epb.plan.estimation.step.pattern.StepPatternCostEstimator;
import com.kayhut.fuse.epb.plan.statistics.StatisticsProvider;
import com.kayhut.fuse.model.execution.plan.Plan;
import com.kayhut.fuse.model.execution.plan.PlanOpBase;
import com.kayhut.fuse.model.execution.plan.PlanWithCost;
import com.kayhut.fuse.model.execution.plan.costs.CountEstimatesCost;
import com.kayhut.fuse.model.execution.plan.costs.PlanDetailedCost;

import java.util.Map;
import java.util.Optional;

/**
 * Created by moti on 29/05/2017.
 */
public class CompositeStepCostEstimator implements StepCostEstimator<Plan, PlanDetailedCost, CountEstimatesCost, StatisticsPatternContext> {
    //region Constructors
    public CompositeStepCostEstimator(
            Map<StatisticsCostEstimator.Pattern,
                    StepPatternCostEstimator<Plan, PlanDetailedCost, CountEstimatesCost, StatisticsPatternContext>> patternEstimators) {
        this.patternEstimators = patternEstimators;
    }
    //endregion

    //region StepCostEstimator Implementation
    @Override
    public Result<Plan, CountEstimatesCost> estimate(
            Optional<PlanWithCost<Plan, PlanDetailedCost>> previousCost,
            StatisticsPatternContext context) {
        return patternEstimators.get(context.getPattern()).estimate(previousCost, context);
    }
    //endregion

    //region Fields
    protected Map<StatisticsCostEstimator.Pattern,
            StepPatternCostEstimator<Plan, PlanDetailedCost, CountEstimatesCost, StatisticsPatternContext>> patternEstimators;
    //endregion
}
