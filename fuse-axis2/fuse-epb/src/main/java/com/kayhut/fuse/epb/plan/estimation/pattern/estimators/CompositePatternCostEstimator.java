package com.kayhut.fuse.epb.plan.estimation.pattern.estimators;

import com.kayhut.fuse.epb.plan.estimation.pattern.Pattern;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by moti on 29/05/2017.
 */
public class CompositePatternCostEstimator<P, C, TContext> implements PatternCostEstimator<P, C, TContext> {
    //region Constructors
    public CompositePatternCostEstimator(Map<Class<? extends Pattern>, PatternCostEstimator<P, C, TContext>> estimators) {
        this.estimators = new HashMap<>(estimators);
    }
    //endregion

    //region PatternCostEstimator Implementation
    @Override
    public Result<P, C> estimate(Pattern pattern, TContext context) {
        PatternCostEstimator<P, C, TContext> estimator = this.estimators.get(pattern.getClass());
        if (estimator == null) {
            return PatternCostEstimator.EmptyResult.get();
        }

        return estimator.estimate(pattern, context);
    }
    //endregion

    //region Fields
    private Map<Class<? extends Pattern>, PatternCostEstimator<P, C, TContext>> estimators;
    //endregion
}
