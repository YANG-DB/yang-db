package com.kayhut.fuse.epb.plan;

/**
 * Created by moti on 2/22/2017.
 */
public interface CostCalculator<P,C> {
    C calculateCost(P plan);
}