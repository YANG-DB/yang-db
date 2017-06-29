package com.kayhut.fuse.epb.plan.estimation.step.context;

import com.kayhut.fuse.epb.plan.estimation.step.StatisticsCostEstimator;
import com.kayhut.fuse.epb.plan.statistics.StatisticsProvider;
import com.kayhut.fuse.model.execution.plan.Plan;
import com.kayhut.fuse.model.execution.plan.PlanOpBase;
import com.kayhut.fuse.model.execution.plan.PlanWithCost;
import com.kayhut.fuse.model.execution.plan.costs.PlanDetailedCost;

import java.util.Map;
import java.util.Optional;

/**
 * Created by Roman on 29/06/2017.
 */
public class M1StepPatternCostEstimatorContext {
    //region Constructors
    public M1StepPatternCostEstimatorContext(
            StatisticsProvider statisticsProvider,
            Map<StatisticsCostEstimator.PatternPart, PlanOpBase> patternParts,
            StatisticsCostEstimator.Pattern pattern,
            Optional<PlanWithCost<Plan, PlanDetailedCost>> previousCost) {
        this.statisticsProvider = statisticsProvider;
        this.patternParts = patternParts;
        this.pattern = pattern;
        this.previousCost = previousCost;
    }
    //endregion

    //region Properties
    public StatisticsProvider getStatisticsProvider() {
        return statisticsProvider;
    }

    public Map<StatisticsCostEstimator.PatternPart, PlanOpBase> getPatternParts() {
        return patternParts;
    }

    public StatisticsCostEstimator.Pattern getPattern() {
        return pattern;
    }

    public Optional<PlanWithCost<Plan, PlanDetailedCost>> getPreviousCost() {
        return previousCost;
    }

    //endregion

    //region Fields
    private StatisticsProvider statisticsProvider;
    private Map<StatisticsCostEstimator.PatternPart, PlanOpBase> patternParts;
    private StatisticsCostEstimator.Pattern pattern;
    private Optional<PlanWithCost<Plan, PlanDetailedCost>> previousCost;
    //endregion
}
