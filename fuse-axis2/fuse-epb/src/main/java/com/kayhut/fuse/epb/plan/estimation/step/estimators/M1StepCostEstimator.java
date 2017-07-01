package com.kayhut.fuse.epb.plan.estimation.step.estimators;

import com.google.inject.Inject;
import com.kayhut.fuse.dispatcher.ontolgy.OntologyProvider;
import com.kayhut.fuse.epb.plan.estimation.CostEstimationConfig;
import com.kayhut.fuse.epb.plan.estimation.step.*;
import com.kayhut.fuse.epb.plan.estimation.step.context.IncrementalCostContext;
import com.kayhut.fuse.epb.plan.estimation.step.context.M1StepCostEstimatorContext;
import com.kayhut.fuse.epb.plan.statistics.StatisticsProviderFactory;
import com.kayhut.fuse.model.asgQuery.AsgQuery;
import com.kayhut.fuse.model.execution.plan.Plan;
import com.kayhut.fuse.model.execution.plan.costs.CountEstimatesCost;
import com.kayhut.fuse.model.execution.plan.costs.PlanDetailedCost;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by moti on 29/05/2017.
 */
public class M1StepCostEstimator extends CompositeStepCostEstimator<Plan, CountEstimatesCost, IncrementalCostContext<Plan, PlanDetailedCost, AsgQuery>> {
    //region Static
    private static Map<Class<? extends Step>,
            StepCostEstimator<Plan, CountEstimatesCost, IncrementalCostContext<Plan, PlanDetailedCost, AsgQuery>>> estimators(
                    CostEstimationConfig config,
                    StatisticsProviderFactory statisticsProviderFactory,
                    OntologyProvider ontologyProvider) {
        Map<Class<? extends Step>, StepCostEstimator<Plan, CountEstimatesCost, IncrementalCostContext<Plan, PlanDetailedCost, AsgQuery>>> estimators =
                new HashMap<>();

        estimators.put(EntityStep.class, new EntityStepCostEstimator(statisticsProviderFactory, ontologyProvider));
        estimators.put(EntityRelationEntityStep.class, new EntityRelationEntityStepCostEstimator(config, statisticsProviderFactory, ontologyProvider));
        estimators.put(GoToEntityRelationEntityStep.class, new GoToEntityRelationEntityStepCostEstimator(config, statisticsProviderFactory, ontologyProvider));

        return estimators;
    }
    //endregion

    //region Constructors
    @Inject
    public M1StepCostEstimator(
            CostEstimationConfig config,
            StatisticsProviderFactory statisticsProviderFactory,
            OntologyProvider ontologyProvider) {
        super(estimators(config, statisticsProviderFactory, ontologyProvider));
    }
    //endregion
}
