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


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Cost cost1 = (Cost) o;

        if (Double.compare(cost1.cost, cost) != 0) return false;
        return total == cost1.total;
    }

    @Override
    public int hashCode() {
        int result;
        long temp;
        temp = Double.doubleToLongBits(cost);
        result = (int) (temp ^ (temp >>> 32));
        result = 31 * result + (int) (total ^ (total >>> 32));
        return result;
    }

    public static Cost of(double cost, long total) {
        return new Cost(cost,total);
    }

}
