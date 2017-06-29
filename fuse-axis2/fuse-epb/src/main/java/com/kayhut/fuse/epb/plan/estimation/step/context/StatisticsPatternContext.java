package com.kayhut.fuse.epb.plan.estimation.step.context;

import com.kayhut.fuse.epb.plan.estimation.step.StatisticsCostEstimator;
import com.kayhut.fuse.epb.plan.statistics.StatisticsProvider;
import com.kayhut.fuse.model.execution.plan.PlanOpBase;

import java.util.Map;

/**
 * Created by Roman on 29/06/2017.
 */
public class StatisticsPatternContext {
    //region Constructors
    public StatisticsPatternContext(
            StatisticsProvider statisticsProvider,
            Map<StatisticsCostEstimator.PatternPart, PlanOpBase> patternParts,
            StatisticsCostEstimator.Pattern pattern) {

        this.statisticsProvider = statisticsProvider;
        this.patternParts = patternParts;
        this.pattern = pattern;
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
    //endregion

    //region Fields
    private StatisticsProvider statisticsProvider;
    private Map<StatisticsCostEstimator.PatternPart, PlanOpBase> patternParts;
    private StatisticsCostEstimator.Pattern pattern;
    //endregion
}
