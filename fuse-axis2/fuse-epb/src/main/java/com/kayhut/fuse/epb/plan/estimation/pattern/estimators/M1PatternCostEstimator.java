package com.kayhut.fuse.epb.plan.estimation.pattern.estimators;

import com.google.inject.Inject;
import com.kayhut.fuse.dispatcher.ontolgy.OntologyProvider;
import com.kayhut.fuse.epb.plan.estimation.CostEstimationConfig;
import com.kayhut.fuse.epb.plan.estimation.pattern.*;
import com.kayhut.fuse.epb.plan.estimation.IncrementalEstimationContext;
import com.kayhut.fuse.epb.plan.statistics.StatisticsProviderFactory;
import com.kayhut.fuse.model.asgQuery.AsgQuery;
import com.kayhut.fuse.model.execution.plan.Plan;
import com.kayhut.fuse.model.execution.plan.costs.Cost;
import com.kayhut.fuse.model.execution.plan.costs.CountEstimatesCost;
import com.kayhut.fuse.model.execution.plan.costs.PlanDetailedCost;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by moti on 29/05/2017.
 */
public class M1PatternCostEstimator extends CompositePatternCostEstimator<Plan, Cost, IncrementalEstimationContext<Plan, PlanDetailedCost, AsgQuery>> {
    //region Static
    private static Map<Class<? extends Pattern>,
            PatternCostEstimator<Plan, Cost, IncrementalEstimationContext<Plan, PlanDetailedCost, AsgQuery>>> estimators(
                    CostEstimationConfig config,
                    StatisticsProviderFactory statisticsProviderFactory,
                    OntologyProvider ontologyProvider) {
        Map<Class<? extends Pattern>, PatternCostEstimator<Plan, Cost, IncrementalEstimationContext<Plan, PlanDetailedCost, AsgQuery>>> estimators =
                new HashMap<>();

        estimators.put(EntityPattern.class, new EntityPatternCostEstimator(statisticsProviderFactory, ontologyProvider));
        estimators.put(EntityRelationEntityPattern.class, new EntityRelationEntityPatternCostEstimator(config, statisticsProviderFactory, ontologyProvider));
        estimators.put(GoToEntityRelationEntityPattern.class,
                new GoToEntityRelationEntityPatternCostEstimator(
                        (EntityRelationEntityPatternCostEstimator)estimators.get(EntityRelationEntityPattern.class)));

        return estimators;
    }
    //endregion

    //region Constructors
    @Inject
    public M1PatternCostEstimator(
            CostEstimationConfig config,
            StatisticsProviderFactory statisticsProviderFactory,
            OntologyProvider ontologyProvider) {
        super(estimators(config, statisticsProviderFactory, ontologyProvider));
    }
    //endregion
}
