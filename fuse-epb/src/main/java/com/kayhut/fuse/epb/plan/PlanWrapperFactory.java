package com.kayhut.fuse.epb.plan;

/**
 * Created by moti on 2/22/2017.
 */
public interface PlanWrapperFactory<P, C> {
    PlanWrapper<P,C> wrapPlan(P extendedPlan);
}
