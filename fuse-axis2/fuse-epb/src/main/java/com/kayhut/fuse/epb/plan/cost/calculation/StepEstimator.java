package com.kayhut.fuse.epb.plan.cost.calculation;

/**
 * Created by liorp on 4/24/2017.
 */
public class StepEstimator {
    public final Double edgeEstimator;
    public final Double outVEstimator;
    public final Double lambda;

    public StepEstimator() {
        this(null,null,null);
    }

    public StepEstimator(Double edgeEstimator, Double outVEstimator, Double lambda) {
        this.edgeEstimator = edgeEstimator;
        this.outVEstimator = outVEstimator;
        this.lambda = lambda;
    }

}
