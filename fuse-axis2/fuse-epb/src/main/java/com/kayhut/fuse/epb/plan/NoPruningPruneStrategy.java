package com.kayhut.fuse.epb.plan;

import java.util.List;

/**
 * Created by moti on 2/23/2017.
 */
public class NoPruningPruneStrategy<P,C> implements PlanPruneStrategy<P,C>{
    @Override
    public Iterable<PlanWrapper<P, C>> prunePlans(Iterable<PlanWrapper<P, C>> plans) {
        return plans;
    }
}
