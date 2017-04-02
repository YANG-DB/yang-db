package com.kayhut.fuse.epb.plan.cost.calculation;

/**
 * Created by moti on 31/03/2017.
 */
public interface CostCalculator<C, I> {
    C calculateCost(I item);
}
