package com.kayhut.fuse.model.execution.plan.costs;

import java.util.Stack;

/**
 * Created by Roman on 27/06/2017.
 */
public class CountEstimatesCost implements Cost {
    //region Constructors
    public CountEstimatesCost(double cost, double countEstimate) {
        this.cost = cost;
        this.countEstimates = new Stack<>();
        this.countEstimates.push(countEstimate);
    }

    public CountEstimatesCost(double cost, Stack<Double> countEstimates) {
        this.cost = cost;
        this.countEstimates = (Stack<Double>) countEstimates.clone();
    }
    //endregion

    //region Public Methods
    public double push(double value) {
        return countEstimates.push(value);
    }

    public double peek() {
        return countEstimates.peek();
    }
    //endregion

    //region Override Methods
    @Override
    public String toString() {
        return "[estimation = " + cost + ", countEstimate = " + this.countEstimates.peek() + "]";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        CountEstimatesCost other = (CountEstimatesCost) o;

        return cost == other.getCost() && countEstimates.equals(other.getCountEstimates());
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
    //endregion

    //region Properties
    public double getCost() {
        return cost;
    }

    public Stack<Double> getCountEstimates() {
        return countEstimates;
    }
    //endregion

    //region Fields
    private double cost;
    private Stack<Double> countEstimates;
    //endregion
}
