package com.kayhut.fuse.epb.plan.estimation.step.pattern;

import com.kayhut.fuse.epb.plan.estimation.step.Step;
import com.kayhut.fuse.epb.plan.estimation.step.StepCostEstimator;
import com.kayhut.fuse.epb.plan.estimation.step.context.M1StepPatternCostEstimatorContext;
import com.kayhut.fuse.model.execution.plan.*;
import com.kayhut.fuse.model.execution.plan.costs.CountEstimatesCost;
import com.kayhut.fuse.model.execution.plan.costs.PlanDetailedCost;

import java.util.Optional;

import static com.kayhut.fuse.epb.plan.estimation.step.StatisticsCostEstimator.PatternPart.ENTITY_ONLY;
import static com.kayhut.fuse.epb.plan.estimation.step.StatisticsCostEstimator.PatternPart.OPTIONAL_ENTITY_ONLY_FILTER;

/**
 * Created by moti on 29/05/2017.
 */
public class SingleEntityStepPatternCostEstimator implements StepPatternCostEstimator<Plan, CountEstimatesCost, M1StepPatternCostEstimatorContext> {
    //region StepPatternCostEstimator Implementation
    @Override
    public StepCostEstimator.Result<Plan, CountEstimatesCost> estimate(Step step, M1StepPatternCostEstimatorContext context) {
        //estimate
        double entityTotal = context.getStatisticsProvider().getNodeStatistics(step.start()._1().getAsgEBase().geteBase()).getTotal();
        double filterTotal = entityTotal;
        if (step.start()._2().getAsgEBase() != null) {
            filterTotal = context.getStatisticsProvider().getNodeFilterStatistics(step.start()._1().getAsgEBase().geteBase(), step.start()._2().getAsgEBase().geteBase()).getTotal();
        }

        double min = Math.min(entityTotal, filterTotal);
        return StepCostEstimator.Result.of(1.0, new PlanWithCost<>(new Plan(step.start()._1(), step.start()._2()), new CountEstimatesCost(min, min)));
    }
    //endregion
}
