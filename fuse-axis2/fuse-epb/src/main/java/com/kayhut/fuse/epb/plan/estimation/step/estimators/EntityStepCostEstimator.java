package com.kayhut.fuse.epb.plan.estimation.step.pattern;

import com.kayhut.fuse.epb.plan.estimation.step.EntityStep;
import com.kayhut.fuse.epb.plan.estimation.step.Step;
import com.kayhut.fuse.epb.plan.estimation.step.StepCostEstimator;
import com.kayhut.fuse.epb.plan.estimation.step.context.M1StepCostEstimatorContext;
import com.kayhut.fuse.model.execution.plan.*;
import com.kayhut.fuse.model.execution.plan.costs.CountEstimatesCost;

/**
 * Created by moti on 29/05/2017.
 */
public class EntityStepCostEstimator implements StepCostEstimator<Plan, CountEstimatesCost, M1StepCostEstimatorContext> {
    //region StepPatternCostEstimator Implementation
    @Override
    public StepCostEstimator.Result<Plan, CountEstimatesCost> estimate(Step step, M1StepCostEstimatorContext context) {
        if (!step.getClass().equals(EntityStep.class)) {
            return StepCostEstimator.EmptyResult.get();
        }

        EntityStep entityStep = (EntityStep)step;
        EntityOp start = entityStep.getStart();
        EntityFilterOp startFilter = entityStep.getStartFilter();

        //estimate
        double entityTotal = context.getStatisticsProvider().getNodeStatistics(start.getAsgEBase().geteBase()).getTotal();
        double filterTotal = entityTotal;
        if (startFilter.getAsgEBase() != null) {
            filterTotal = context.getStatisticsProvider().getNodeFilterStatistics(start.getAsgEBase().geteBase(), startFilter.getAsgEBase().geteBase()).getTotal();
        }

        double min = Math.min(entityTotal, filterTotal);
        return StepCostEstimator.Result.of(1.0, new PlanWithCost<>(new Plan(start, startFilter), new CountEstimatesCost(min, min)));
    }
    //endregion
}
