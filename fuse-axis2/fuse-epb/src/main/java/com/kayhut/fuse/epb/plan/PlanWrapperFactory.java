package com.kayhut.fuse.epb.plan;

/**
 * Created by moti on 2/22/2017.
 */
public interface PlanWrapperFactory<P, Q, C>{
    PlanWrapper<P,C> wrapPlan(P extendedPlan, Q query);
}
