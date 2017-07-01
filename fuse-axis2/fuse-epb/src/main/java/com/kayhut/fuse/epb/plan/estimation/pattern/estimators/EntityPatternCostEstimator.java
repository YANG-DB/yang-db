package com.kayhut.fuse.epb.plan.estimation.step.estimators;

import com.kayhut.fuse.dispatcher.ontolgy.OntologyProvider;
import com.kayhut.fuse.epb.plan.estimation.step.EntityStep;
import com.kayhut.fuse.epb.plan.estimation.step.Step;
import com.kayhut.fuse.epb.plan.estimation.step.StepCostEstimator;
import com.kayhut.fuse.epb.plan.estimation.step.context.IncrementalCostContext;
import com.kayhut.fuse.epb.plan.estimation.step.context.M1StepCostEstimatorContext;
import com.kayhut.fuse.epb.plan.statistics.StatisticsProvider;
import com.kayhut.fuse.epb.plan.statistics.StatisticsProviderFactory;
import com.kayhut.fuse.model.asgQuery.AsgQuery;
import com.kayhut.fuse.model.execution.plan.*;
import com.kayhut.fuse.model.execution.plan.costs.CountEstimatesCost;
import com.kayhut.fuse.model.execution.plan.costs.PlanDetailedCost;

/**
 * Created by moti on 29/05/2017.
 */
public class EntityStepCostEstimator implements StepCostEstimator<Plan, CountEstimatesCost, IncrementalCostContext<Plan, PlanDetailedCost, AsgQuery>> {
    //region Constructors
    public EntityStepCostEstimator(StatisticsProviderFactory statisticsProviderFactory, OntologyProvider ontologyProvider) {
        this.statisticsProviderFactory = statisticsProviderFactory;
        this.ontologyProvider = ontologyProvider;
    }
    //endregion

    //region StepPatternCostEstimator Implementation
    @Override
    public StepCostEstimator.Result<Plan, CountEstimatesCost> estimate(
            Step step,
            IncrementalCostContext<Plan, PlanDetailedCost, AsgQuery> context) {

        if (!step.getClass().equals(EntityStep.class)) {
            return StepCostEstimator.EmptyResult.get();
        }

        EntityStep entityStep = (EntityStep)step;
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
        return StepCostEstimator.Result.of(1.0, new PlanWithCost<>(new Plan(start, startFilter), new CountEstimatesCost(min, min)));
    }
    //endregion

    //region Fields
    private StatisticsProviderFactory statisticsProviderFactory;
    private OntologyProvider ontologyProvider;
    //endregion
}
