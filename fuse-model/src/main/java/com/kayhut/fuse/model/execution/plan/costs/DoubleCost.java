package com.kayhut.fuse.model.execution.plan.costs;

/**
 * Created by moti on 4/20/2017.
 */
public class DoubleCost implements Cost {
    //region Static
    public static DoubleCost of(double cost) {
        return new DoubleCost(cost);
    }
    //endregion

    //region Constructors
    public DoubleCost(double cost) {
        this.cost = cost;
    }
    //endregion

    //region Properties
    public double getCost() {
        return cost;
    }
    //endregion

    //region Override Methods
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DoubleCost cost1 = (DoubleCost) o;

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
        return "DoubleCost{" +
                "cost=" + cost +
                '}';
    }
    //endregion

    //region Fields
    public double cost;
    //endregion
}
