package com.kayhut.fuse.epb.plan;

/**
 * Created by moti on 2/23/2017.
 */
public class NoPruningPruneStrategy<P> implements PlanPruneStrategy<P>{
    @Override
    public Iterable<P> prunePlans(Iterable<P> plans) {
        return plans;
    }
}
