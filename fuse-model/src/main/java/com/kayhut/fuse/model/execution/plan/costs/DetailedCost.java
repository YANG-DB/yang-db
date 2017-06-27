package com.kayhut.fuse.model.execution.plan.costs;

/**
 * Created by moti on 5/18/2017.
 */
public class DetailedCost extends DoubleCost {

    public final double lambdaNode;
    public final double lambdaEdge;
    public final double R;
    public final double N2;

    public DetailedCost(double cost, double lambdaNode , double lambdaEdge , double R, double N2) {
        super(cost);
        this.lambdaNode = lambdaNode;
        this.lambdaEdge = lambdaEdge;
        this.R = R;
        this.N2 = N2;
    }

    @Override
    public String toString() {
        return "{" +
                "cost=" + cost +
                "lambdaNode=" + lambdaNode +
                ", lambdaEdge=" + lambdaEdge +
                ", R=" + R +
                ", N2=" + N2 +
                '}';
    }
}
