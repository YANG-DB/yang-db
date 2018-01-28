package com.kayhut.fuse.epb.plan.estimation.pattern.estimators;

import com.kayhut.fuse.epb.plan.estimation.pattern.Pattern;
import com.kayhut.fuse.model.execution.plan.PlanWithCost;

import java.util.*;

/**
 * Created by liorp on 4/24/2017.
 */
public interface PatternCostEstimator<P1, C1, TContext> {
    interface EmptyResult<P3, C3> extends Result<P3, C3> {
        static <P2, C2> Result<P2, C2> get() {
            return new EmptyResult<P2, C2>() {
                @Override
                public List<PlanWithCost<P2, C2>> getPlanStepCosts() {
                    return Collections.emptyList();
                }

                @Override
                public double[] countsUpdateFactors() {
                    return new double[] { 0 };
                }
            };
        }
    }

    interface Result<P2, C2> {
        List<PlanWithCost<P2, C2>> getPlanStepCosts();

        double[] countsUpdateFactors();

        @SafeVarargs
        static <P2, C2> Result<P2, C2> of(double[] countsUpdateFactors, PlanWithCost<P2, C2> ... planStepCosts) {
            return new Result<P2, C2>() {
                @Override
                public List<PlanWithCost<P2, C2>> getPlanStepCosts() {
                    return Arrays.asList(planStepCosts);
                }

                @Override
                public double[] countsUpdateFactors() {
                    return countsUpdateFactors;
                }
            };
        }
    }


    Result<P1, C1> estimate(Pattern pattern, TContext context);
}
