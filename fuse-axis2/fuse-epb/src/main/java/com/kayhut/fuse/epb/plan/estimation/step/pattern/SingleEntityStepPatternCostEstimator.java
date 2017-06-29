package com.kayhut.fuse.epb.plan.estimation.step.pattern;

import com.kayhut.fuse.epb.plan.estimation.step.StatisticsCostEstimator;
import com.kayhut.fuse.epb.plan.estimation.step.StepCostEstimator;
import com.kayhut.fuse.epb.plan.estimation.step.context.StatisticsPatternContext;
import com.kayhut.fuse.epb.plan.statistics.StatisticsProvider;
import com.kayhut.fuse.model.execution.plan.*;
import com.kayhut.fuse.model.execution.plan.costs.CountEstimatesCost;
import com.kayhut.fuse.model.execution.plan.costs.PlanDetailedCost;

import java.util.Map;
import java.util.Optional;

import static com.kayhut.fuse.epb.plan.estimation.step.StatisticsCostEstimator.PatternPart.ENTITY_ONLY;
import static com.kayhut.fuse.epb.plan.estimation.step.StatisticsCostEstimator.PatternPart.OPTIONAL_ENTITY_ONLY_FILTER;

/**
 * Created by moti on 29/05/2017.
 */
public class SingleEntityStepPatternCostEstimator implements StepPatternCostEstimator<Plan, PlanDetailedCost, CountEstimatesCost, StatisticsPatternContext> {
    //region StepPatternCostEstimator Implementation
    @Override
    public StepCostEstimator.Result<Plan, CountEstimatesCost> estimate(
            Optional<PlanWithCost<Plan, PlanDetailedCost>> previousCost,
            StatisticsPatternContext context) {

        EntityOp entityOp = (EntityOp) context.getPatternParts().get(ENTITY_ONLY);
        if (!context.getPatternParts().containsKey(OPTIONAL_ENTITY_ONLY_FILTER)) {
            context.getPatternParts().put(OPTIONAL_ENTITY_ONLY_FILTER, new EntityFilterOp());
        }

        EntityFilterOp filterOp = (EntityFilterOp) context.getPatternParts().get(OPTIONAL_ENTITY_ONLY_FILTER);
        //set entity type on this kaka
        filterOp.setEntity(entityOp.getAsgEBase());
        //estimate
        double entityTotal = context.getStatisticsProvider().getNodeStatistics(entityOp.getAsgEBase().geteBase()).getTotal();
        double filterTotal = entityTotal;
        if (filterOp.getAsgEBase() != null) {
            filterTotal = context.getStatisticsProvider().getNodeFilterStatistics(entityOp.getAsgEBase().geteBase(), filterOp.getAsgEBase().geteBase()).getTotal();
        }

        double min = Math.min(entityTotal, filterTotal);
        return StepCostEstimator.Result.of(1.0, new PlanWithCost<>(new Plan(entityOp, filterOp), new CountEstimatesCost(min, min)));
    }
    //endregion
}
