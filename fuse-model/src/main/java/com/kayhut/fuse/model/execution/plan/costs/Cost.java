package com.kayhut.fuse.model.execution.plan.costs;

/**
 * Created by moti on 4/20/2017.
 */
public class Cost implements ICost{
    public Cost(double cost) {
        this.cost = cost;
    }

    public double cost;
    //public long cardinality;


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Cost cost1 = (Cost) o;

        return (Double.compare(cost1.cost, cost) == 0) ;
    }

    @Override
    public int hashCode() {
        int result;
        long temp;
        temp = Double.doubleToLongBits(cost);
        result = (int) (temp ^ (temp >>> 32));
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        return result;
    }

    @Override
    public String toString() {
        return "Cost{" +
                "cost=" + cost +
                '}';
    }

    public static Cost of(double cost) {
        return new Cost(cost);
    }


}
