package com.kayhut.fuse.epb.plan.cost.calculation;

import com.kayhut.fuse.epb.plan.cost.StatisticsCostEstimator;
import com.kayhut.fuse.epb.plan.statistics.StatisticsProvider;
import com.kayhut.fuse.model.execution.plan.Plan;
import com.kayhut.fuse.model.execution.plan.PlanOpBase;
import com.kayhut.fuse.model.execution.plan.PlanOpWithCost;
import com.kayhut.fuse.model.execution.plan.PlanWithCost;
import com.kayhut.fuse.model.execution.plan.costs.CountEstimatesCost;
import com.kayhut.fuse.model.execution.plan.costs.DoubleCost;
import com.kayhut.fuse.model.execution.plan.costs.PlanDetailedCost;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Created by liorp on 4/24/2017.
 */
public interface StepEstimator {

    StepEstimatorResult calculate(StatisticsProvider statisticsProvider, Map<StatisticsCostEstimator.StatisticsCostEstimatorNames, PlanOpBase> map,
                                  StatisticsCostEstimator.StatisticsCostEstimatorPatterns pattern,
                                  Optional<PlanWithCost<Plan, PlanDetailedCost>> previousCost);

    interface StepEstimatorResult {
        List<PlanWithCost<Plan, CountEstimatesCost>> getPlanStepCosts();

        double lambda();

        @SafeVarargs
        static StepEstimatorResult of(double lambda, PlanWithCost<Plan, CountEstimatesCost> ... planStepCosts) {
            return new StepEstimatorResult() {
                @Override
                public List<PlanWithCost<Plan, CountEstimatesCost>> getPlanStepCosts() {
                    return Arrays.asList(planStepCosts);
                }

                @Override
                public double lambda() {
                    return lambda;
                }
            };
        }
    }

}
