package com.kayhut.fuse.epb.plan.estimation.step;

import com.kayhut.fuse.epb.plan.statistics.StatisticsProvider;
import com.kayhut.fuse.model.execution.plan.Plan;
import com.kayhut.fuse.model.execution.plan.PlanOpBase;
import com.kayhut.fuse.model.execution.plan.PlanWithCost;
import com.kayhut.fuse.model.execution.plan.costs.CountEstimatesCost;
import com.kayhut.fuse.model.execution.plan.costs.PlanDetailedCost;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Created by liorp on 4/24/2017.
 */
public interface StepCostEstimator<P1, C1, TContext> {

    Result<P1, C1> estimate(Step step, TContext context);

    interface Result<P2, C2> {
        List<PlanWithCost<P2, C2>> getPlanStepCosts();

        double lambda();

        @SafeVarargs
        static <P2, C2> Result<P2, C2> of(double lambda, PlanWithCost<P2, C2> ... planStepCosts) {
            return new Result<P2, C2>() {
                @Override
                public List<PlanWithCost<P2, C2>> getPlanStepCosts() {
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
