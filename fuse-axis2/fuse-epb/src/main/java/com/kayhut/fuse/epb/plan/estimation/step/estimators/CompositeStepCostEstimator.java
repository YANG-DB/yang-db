package com.kayhut.fuse.epb.plan.estimation.step.estimators;

import com.kayhut.fuse.epb.plan.estimation.step.Step;
import com.kayhut.fuse.epb.plan.estimation.step.StepCostEstimator;
import javaslang.collection.Stream;

import java.util.Map;
import java.util.Optional;

/**
 * Created by moti on 29/05/2017.
 */
public class CompositeStepCostEstimator<P, C, TContext> implements StepCostEstimator<P, C, TContext> {
    //region Constructors
    public CompositeStepCostEstimator(StepCostEstimator<P, C, TContext>...estimators) {
        this.estimators = Stream.of(estimators);
    }

    public CompositeStepCostEstimator(Iterable<StepCostEstimator<P, C, TContext>> estimators) {
        this.estimators = Stream.ofAll(estimators).toJavaList();
    }
    //endregion

    //region StepCostEstimator Implementation
    @Override
    public Result<P, C> estimate(Step step, TContext context) {
        for(StepCostEstimator<P, C, TContext> estimator : this.estimators) {
            Result<P, C> result = estimator.estimate(step, context);
            if (!(result instanceof EmptyResult)) {
                return result;
            }
        }

        return StepCostEstimator.EmptyResult.get();
    }
    //endregion

    //region Fields
    private Iterable<StepCostEstimator<P, C, TContext>> estimators;
    //endregion
}
