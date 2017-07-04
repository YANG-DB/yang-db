package com.kayhut.fuse.epb.plan;

import com.kayhut.fuse.model.asgQuery.IQuery;
import com.kayhut.fuse.model.execution.plan.IPlan;

import java.util.Optional;

public interface PlanSeedStrategy<P extends IPlan,Q extends IQuery> {
    Iterable<P> extendPlan(Q query);
}
