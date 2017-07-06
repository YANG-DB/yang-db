package com.kayhut.fuse.model.execution.plan.costs;

/**
 * Created by moti on 6/21/2017.
 */
public interface Cost extends Cloneable{
    Object clone();
    double getCost();
}
