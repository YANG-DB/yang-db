package com.kayhut.fuse.epb.plan.estimation.step.estimators;

import com.kayhut.fuse.epb.plan.estimation.step.Step;
import com.kayhut.fuse.epb.plan.estimation.step.StepCostEstimator;
import javaslang.collection.Stream;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Created by moti on 29/05/2017.
 */
public class CompositeStepCostEstimator<P, C, TContext> implements StepCostEstimator<P, C, TContext> {
    //region Constructors
    public CompositeStepCostEstimator(Map<Class<? extends Step>, StepCostEstimator<P, C, TContext>> estimators) {
        this.estimators = new HashMap<>(estimators);
    }
    //endregion

    //region StepCostEstimator Implementation
    @Override
    public Result<P, C> estimate(Step step, TContext context) {
        StepCostEstimator<P, C, TContext> estimator = this.estimators.get(step.getClass());
        if (estimator == null) {
            return StepCostEstimator.EmptyResult.get();
        }

        return estimator.estimate(step, context);
    }
    //endregion

    //region Fields
    private Map<Class<? extends Step>, StepCostEstimator<P, C, TContext>> estimators;
    //endregion
}
