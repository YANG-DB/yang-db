package com.kayhut.fuse.epb.plan.extenders;

import com.kayhut.fuse.epb.plan.PlanExtensionStrategy;

import java.util.List;

/**
 * Created by moti on 2/27/2017.
 */
public class CompositePlanExtensionStrategy<P,Q> implements PlanExtensionStrategy<P,Q> {

    private List<PlanExtensionStrategy<P,Q>> innerExtenders;

    public CompositePlanExtensionStrategy(List<PlanExtensionStrategy<P, Q>> innerExtenders) {
        this.innerExtenders = innerExtenders;
    }

    @Override
    public Iterable<P> extendPlan(P plan, Q query) {
        return null;
    }
}
