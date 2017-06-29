package com.kayhut.fuse.epb.plan.estimation.step;

import com.kayhut.fuse.epb.plan.estimation.step.pattern.StepPatternCostEstimator;
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
public class CompositeStepCostEstimator implements StepCostEstimator {
    //region Constructors
    public CompositeStepCostEstimator(Map<StatisticsCostEstimator.Pattern, StepPatternCostEstimator> patternEstimators) {
        this.patternEstimators = patternEstimators;
    }
    //endregion

    //region StepCostEstimator Implementation
    @Override
    public Result estimate(
            StatisticsProvider statisticsProvider,
            Map<StatisticsCostEstimator.Token, PlanOpBase> patternParts,
            StatisticsCostEstimator.Pattern pattern,
            Optional<PlanWithCost<Plan, PlanDetailedCost>> previousCost) {
        return patternEstimators.get(pattern).estimate(statisticsProvider, patternParts, previousCost);
    }
    //endregion

    //region Fields
    protected Map<StatisticsCostEstimator.Pattern, StepPatternCostEstimator> patternEstimators;
    //endregion
}
