package com.kayhut.fuse.epb.plan.cost.calculation;

/**
 * Created by moti on 6/1/2017.
 */
public final class CostEstimationConfig {
    private double alpha;
    private double delta;

    public CostEstimationConfig(double alpha,double delta) {
        this.alpha = alpha;
        this.delta = delta;
    }

    public double getAlpha() {
        return alpha;
    }

    public double getDelta() {
        return delta;
    }
}
