package com.kayhut.fuse.dispatcher.epb;

import com.kayhut.fuse.model.execution.plan.IPlan;

/**
 * Created by moti on 2/22/2017.
 */
public interface PlanPruneStrategy <P extends IPlan> {
    Iterable<P> prunePlans(Iterable<P> plans);
}
