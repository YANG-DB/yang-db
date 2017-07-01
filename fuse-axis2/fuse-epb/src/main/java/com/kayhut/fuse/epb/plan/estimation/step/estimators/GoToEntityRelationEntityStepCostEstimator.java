package com.kayhut.fuse.epb.plan.estimation.step.estimators;

import com.kayhut.fuse.dispatcher.ontolgy.OntologyProvider;
import com.kayhut.fuse.epb.plan.estimation.CostEstimationConfig;
import com.kayhut.fuse.epb.plan.estimation.step.GoToEntityRelationEntityStep;
import com.kayhut.fuse.epb.plan.estimation.step.Step;
import com.kayhut.fuse.epb.plan.estimation.step.StepCostEstimator;
import com.kayhut.fuse.epb.plan.estimation.step.context.IncrementalCostContext;
import com.kayhut.fuse.epb.plan.estimation.step.context.M1StepCostEstimatorContext;
import com.kayhut.fuse.epb.plan.statistics.StatisticsProvider;
import com.kayhut.fuse.epb.plan.statistics.StatisticsProviderFactory;
import com.kayhut.fuse.model.asgQuery.AsgQuery;
import com.kayhut.fuse.model.execution.plan.Plan;
import com.kayhut.fuse.model.execution.plan.PlanWithCost;
import com.kayhut.fuse.model.execution.plan.costs.CountEstimatesCost;
import com.kayhut.fuse.model.execution.plan.costs.PlanDetailedCost;

/**
 * Created by moti on 29/05/2017.
 */
public class GoToEntityRelationEntityStepCostEstimator implements StepCostEstimator<Plan, CountEstimatesCost, IncrementalCostContext<Plan, PlanDetailedCost, AsgQuery>> {
    //region Constructors
    public GoToEntityRelationEntityStepCostEstimator(
            CostEstimationConfig config,
            StatisticsProviderFactory statisticsProviderFactory,
            OntologyProvider ontologyProvider) {
        this.config = config;
        this.statisticsProviderFactory = statisticsProviderFactory;
        this.ontologyProvider = ontologyProvider;
    }
    //endregion

    //region StepPatternCostEstimator Implementation
    @Override
    public StepCostEstimator.Result<Plan, CountEstimatesCost> estimate(
            Step step,
            IncrementalCostContext<Plan, PlanDetailedCost, AsgQuery> context) {
        if (!step.getClass().equals(GoToEntityRelationEntityStep.class)) {
            return StepCostEstimator.EmptyResult.get();
        }

        GoToEntityRelationEntityStep goToEntityRelationEntityStep = (GoToEntityRelationEntityStep)step;

        StatisticsProvider statisticsProvider = this.statisticsProviderFactory.get(this.ontologyProvider.get(context.getQuery().getOnt()).get());

        StepCostEstimator.Result<Plan, CountEstimatesCost> stepEstimatorResult =
                EntityRelationEntityStepCostEstimator.calculateFullStep(
                        config,
                        statisticsProvider,
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
    private StatisticsProviderFactory statisticsProviderFactory;
    private OntologyProvider ontologyProvider;
    //endregion
}
