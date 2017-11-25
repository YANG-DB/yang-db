package com.kayhut.fuse.model.execution.plan.planTree;

import com.kayhut.fuse.model.execution.plan.IPlan;

import java.util.Optional;

/**
 * Created by liorp on 6/26/2017.
 */
public interface BuilderIfc<P extends IPlan> {

    BuilderIfc add(PlanNode child);

    BuilderIfc add(P node, String validationContext);

    int incAndGetPhase();

    BuilderIfc with(P node);

    BuilderIfc selected(Iterable<P> selectedPlans);

    Optional<PlanNode<P>> build();
}
