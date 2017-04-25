package com.kayhut.fuse.model.execution.plan.costs;

/**
 * Created by moti on 4/20/2017.
 */
public class Cost {
    public Cost(double cost, long total) {
        this.cost = cost;
        this.total = total;
        //this.cardinality = cardinality;
    }

    public double cost;
    public long total;
    //public long cardinality;

    public static Cost of(double cost, long total) {
        return new Cost(cost,total);
    }

}
