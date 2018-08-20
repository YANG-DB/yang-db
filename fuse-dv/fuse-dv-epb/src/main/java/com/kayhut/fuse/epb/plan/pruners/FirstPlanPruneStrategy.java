package com.kayhut.fuse.epb.plan.pruners;

import com.kayhut.fuse.dispatcher.epb.PlanPruneStrategy;
import com.kayhut.fuse.model.execution.plan.PlanWithCost;

import java.util.Collections;

/**
 * Created by Roman on 8/20/2018.
 */
public class FirstPlanPruneStrategy<P, C> implements PlanPruneStrategy<PlanWithCost<P, C>> {
    //region PlanPruneStrategy Implementation
    @Override
    public Iterable<PlanWithCost<P, C>> prunePlans(Iterable<PlanWithCost<P, C>> plans) {
        return Collections.singletonList(plans.iterator().next());
    }
    //endegion
}
