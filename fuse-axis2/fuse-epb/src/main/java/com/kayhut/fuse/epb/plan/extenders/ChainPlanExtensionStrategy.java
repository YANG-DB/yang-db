package com.kayhut.fuse.epb.plan.extenders;

import com.google.inject.Inject;
import com.kayhut.fuse.dispatcher.epb.PlanExtensionStrategy;
import com.kayhut.fuse.model.asgQuery.IQuery;
import com.kayhut.fuse.model.execution.plan.IPlan;
import javaslang.collection.Stream;

import java.util.*;
import java.util.logging.Level;

public class ChainPlanExtensionStrategy<P extends IPlan, Q extends IQuery> implements PlanExtensionStrategy<P, Q> {
    //region Constructors
    @Inject
    @SafeVarargs
    public ChainPlanExtensionStrategy(PlanExtensionStrategy<P, Q>...innerExtenders ) {
        this.innerExtenders = Stream.of(innerExtenders).toJavaList();
    }

    @Inject
    public ChainPlanExtensionStrategy(Iterable<PlanExtensionStrategy<P, Q>> innerExtenders) {
        this.innerExtenders = Stream.ofAll(innerExtenders).toJavaList();
    }
    //endregion

    //region PlanExtensionStrategy Implementation
    @Override
    public Iterable<P> extendPlan(Optional<P> plan, Q query) {
        Iterable<Optional<P>> plans = Collections.singletonList(plan);

        for (PlanExtensionStrategy<P, Q> extensionStrategy : this.innerExtenders) {
            plans = Stream.ofAll(plans)
                    .map(childPlan -> extensionStrategy.extendPlan(childPlan, query))
                    .flatMap(childPlans -> childPlans)
                    .map(childPlan -> Optional.of(childPlan))
                    .toJavaList();
        }

        return Stream.ofAll(plans).filter(Optional::isPresent).map(Optional::get);
    }
    //endregion

    //region Fields
    private Iterable<PlanExtensionStrategy<P, Q>> innerExtenders;
    private Level level;
    //endregion
}
