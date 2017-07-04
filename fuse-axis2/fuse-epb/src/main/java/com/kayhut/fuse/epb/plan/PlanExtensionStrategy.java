package com.kayhut.fuse.epb.plan;

import com.kayhut.fuse.model.asgQuery.IQuery;
import com.kayhut.fuse.model.execution.plan.IPlan;

/**
 * Created by moti on 2/21/2017.
 */
public interface PlanExtensionStrategy<P extends IPlan,Q extends IQuery> {
    Iterable<P> extendPlan(P plan, Q query);
}
