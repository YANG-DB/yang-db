package com.kayhut.fuse.epb.plan;

import com.kayhut.fuse.model.asgQuery.AsgQuery;
import com.kayhut.fuse.model.execution.plan.PlanWithCost;

/**
 * Created by moti on 6/21/2017.
 */
public class BottomUpPlanSearcherLoggingDecorator<P,C,Q> implements PlanSearcher<P, C, Q> {
    @Override
    public Iterable<PlanWithCost<P, C>> search(Q query) {
        return null;
    }
}
