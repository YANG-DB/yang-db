package com.kayhut.fuse.epb.plan.estimation.pattern.estimators;

import com.kayhut.fuse.dispatcher.ontolgy.OntologyProvider;
import com.kayhut.fuse.epb.plan.estimation.pattern.EntityPattern;
import com.kayhut.fuse.epb.plan.estimation.pattern.Pattern;
import com.kayhut.fuse.epb.plan.estimation.IncrementalEstimationContext;
import com.kayhut.fuse.epb.plan.statistics.StatisticsProvider;
import com.kayhut.fuse.epb.plan.statistics.StatisticsProviderFactory;
import com.kayhut.fuse.model.asgQuery.AsgQuery;
import com.kayhut.fuse.model.execution.plan.*;
import com.kayhut.fuse.model.execution.plan.costs.Cost;
import com.kayhut.fuse.model.execution.plan.costs.CountEstimatesCost;
import com.kayhut.fuse.model.execution.plan.costs.PlanDetailedCost;

/**
 * Created by moti on 29/05/2017.
 */
public class EntityPatternCostEstimator implements PatternCostEstimator<Plan, Cost, IncrementalEstimationContext<Plan, PlanDetailedCost, AsgQuery>> {
    //region Constructors
    public EntityPatternCostEstimator(StatisticsProviderFactory statisticsProviderFactory, OntologyProvider ontologyProvider) {
        this.statisticsProviderFactory = statisticsProviderFactory;
        this.ontologyProvider = ontologyProvider;
    }
    //endregion

    //region StepPatternCostEstimator Implementation
    @Override
    public PatternCostEstimator.Result<Plan, Cost> estimate(
            Pattern pattern,
            IncrementalEstimationContext<Plan, PlanDetailedCost, AsgQuery> context) {
        if (!EntityPattern.class.isAssignableFrom(pattern.getClass())) {
            return PatternCostEstimator.EmptyResult.get();
        }

        EntityPattern entityStep = (EntityPattern) pattern;
        EntityOp start = entityStep.getStart();
        EntityFilterOp startFilter = entityStep.getStartFilter();

        StatisticsProvider statisticsProvider = this.statisticsProviderFactory.get(this.ontologyProvider.get(context.getQuery().getOnt()).get());

        //estimate
        double entityTotal = statisticsProvider.getNodeStatistics(start.getAsgEBase().geteBase()).getTotal();
        double filterTotal = entityTotal;
        if (startFilter.getAsgEBase() != null) {
            filterTotal = statisticsProvider.getNodeFilterStatistics(start.getAsgEBase().geteBase(), startFilter.getAsgEBase().geteBase()).getTotal();
        }

        double min = Math.min(entityTotal, filterTotal);
        return PatternCostEstimator.Result.of(1.0, new PlanWithCost<>(new Plan(start, startFilter), new CountEstimatesCost(min, min)));
    }
    //endregion

    //region Fields
    private StatisticsProviderFactory statisticsProviderFactory;
    private OntologyProvider ontologyProvider;
    //endregion
}
