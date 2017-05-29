package com.kayhut.fuse.epb.plan.cost.calculation;

import com.kayhut.fuse.dispatcher.utils.PlanUtil;
import com.kayhut.fuse.epb.plan.cost.StatisticsCostEstimator;
import com.kayhut.fuse.epb.plan.statistics.StatisticsProvider;
import com.kayhut.fuse.model.execution.plan.*;
import com.kayhut.fuse.model.execution.plan.costs.Cost;
import com.kayhut.fuse.model.execution.plan.costs.PlanDetailedCost;

import java.util.Map;
import java.util.Optional;

import static com.kayhut.fuse.epb.plan.cost.StatisticsCostEstimator.StatisticsCostEstimatorNames.ENTITY_ONE;
import static com.kayhut.fuse.epb.plan.cost.StatisticsCostEstimator.StatisticsCostEstimatorNames.GOTO_ENTITY;
import static com.kayhut.fuse.epb.plan.cost.StatisticsCostEstimator.StatisticsCostEstimatorNames.OPTIONAL_ENTITY_ONE_FILTER;

/**
 * Created by moti on 29/05/2017.
 */
public class GoToPatternEstimator implements PatternCostEstimator {
    private FullStepPatternEstimator fullStepPatternEstimator;

    public GoToPatternEstimator(FullStepPatternEstimator fullStepPatternEstimator) {
        this.fullStepPatternEstimator = fullStepPatternEstimator;
    }

    @Override
    public StepEstimator.StepEstimatorResult estimate(StatisticsProvider statisticsProvider, Map<StatisticsCostEstimator.StatisticsCostEstimatorNames, PlanOpBase> patternParts, Optional<PlanWithCost<Plan, PlanDetailedCost>> previousCost) {
        GoToEntityOp gotoOp = (GoToEntityOp) patternParts.get(GOTO_ENTITY);

        PlanOpBase entityOp = previousCost.get().getPlan().getOps().stream().
                filter(op -> (op instanceof EntityOp) && ((EntityOp) op).getAsgEBase().geteBase().equals(gotoOp.getAsgEBase().geteBase())).
                findFirst().get();
        EntityFilterOp filterOp = (EntityFilterOp) PlanUtil.adjacentNext(previousCost.get().getPlan(), entityOp).get();

        patternParts.put(ENTITY_ONE, entityOp);
        patternParts.put(OPTIONAL_ENTITY_ONE_FILTER, filterOp);

        StepEstimator.StepEstimatorResult stepEstimatorResult = fullStepPatternEstimator.estimate(statisticsProvider, patternParts, previousCost);
        Cost gotoCost = new Cost(0);

        return StepEstimator.StepEstimatorResult.of(stepEstimatorResult.lambda(), new PlanOpWithCost<Cost>(gotoCost, 0,gotoOp), stepEstimatorResult.planOpWithCosts().get(1), stepEstimatorResult.planOpWithCosts().get(2));

    }
}
