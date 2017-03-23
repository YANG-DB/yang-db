package com.kayhut.fuse.epb.plan;

/**
 * Created by moti on 2/22/2017.
 */
public interface PlanPruneStrategy<P,C> {
    Iterable<PlanWrapper<P,C>> prunePlans(Iterable<PlanWrapper<P,C>> plans);
}
