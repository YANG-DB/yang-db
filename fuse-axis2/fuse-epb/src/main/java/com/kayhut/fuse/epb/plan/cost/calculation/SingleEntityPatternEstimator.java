package com.kayhut.fuse.epb.plan.cost.calculation;

import com.kayhut.fuse.epb.plan.cost.StatisticsCostEstimator;
import com.kayhut.fuse.epb.plan.statistics.StatisticsProvider;
import com.kayhut.fuse.model.execution.plan.*;
import com.kayhut.fuse.model.execution.plan.costs.CountEstimatesCost;
import com.kayhut.fuse.model.execution.plan.costs.DoubleCost;
import com.kayhut.fuse.model.execution.plan.costs.PlanDetailedCost;

import java.util.Map;
import java.util.Optional;

import static com.kayhut.fuse.epb.plan.cost.StatisticsCostEstimator.StatisticsCostEstimatorNames.ENTITY_ONLY;
import static com.kayhut.fuse.epb.plan.cost.StatisticsCostEstimator.StatisticsCostEstimatorNames.OPTIONAL_ENTITY_ONLY_FILTER;

/**
 * Created by moti on 29/05/2017.
 */
public class SingleEntityPatternEstimator implements PatternCostEstimator {
    //region PatternCostEstimator Implementation
    @Override
    public StepEstimator.StepEstimatorResult estimate(StatisticsProvider statisticsProvider, Map<StatisticsCostEstimator.StatisticsCostEstimatorNames, PlanOpBase> patternParts, Optional<PlanWithCost<Plan, PlanDetailedCost>> previousCost) {
        EntityOp entityOp = (EntityOp) patternParts.get(ENTITY_ONLY);
        if (!patternParts.containsKey(OPTIONAL_ENTITY_ONLY_FILTER)) {
            patternParts.put(OPTIONAL_ENTITY_ONLY_FILTER, new EntityFilterOp());
        }

        EntityFilterOp filterOp = (EntityFilterOp) patternParts.get(OPTIONAL_ENTITY_ONLY_FILTER);
        //set entity type on this kaka
        filterOp.setEntity(entityOp.getAsgEBase());
        //calculate
        double entityTotal = statisticsProvider.getNodeStatistics(entityOp.getAsgEBase().geteBase()).getTotal();
        double filterTotal = entityTotal;
        if (filterOp.getAsgEBase() != null) {
            filterTotal = statisticsProvider.getNodeFilterStatistics(entityOp.getAsgEBase().geteBase(), filterOp.getAsgEBase().geteBase()).getTotal();
        }

        double min = Math.min(entityTotal, filterTotal);
        return StepEstimator.StepEstimatorResult.of(1.0, new PlanWithCost<>(new Plan(entityOp, filterOp), new CountEstimatesCost(min, min)));
    }
    //endregion
}
