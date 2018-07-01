package com.kayhut.fuse.epb.plan.extenders;

import com.google.inject.Inject;
import com.kayhut.fuse.dispatcher.epb.PlanExtensionStrategy;
import com.kayhut.fuse.model.asgQuery.IQuery;
import com.kayhut.fuse.model.execution.plan.IPlan;
import javaslang.collection.Stream;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

public class FirstNotEmptyPlanExtensionStrategy<P extends IPlan, Q extends IQuery> implements PlanExtensionStrategy<P , Q> {
    //region Constructors
    @Inject
    @SafeVarargs
    public FirstNotEmptyPlanExtensionStrategy(PlanExtensionStrategy<P, Q> ... innerExtenders) {
        this(Stream.of(innerExtenders));
    }

    public FirstNotEmptyPlanExtensionStrategy(Iterable<PlanExtensionStrategy<P, Q>> innerExtenders) {
        this.innerExtenders = Stream.ofAll(innerExtenders).toJavaList();
    }
    //endregion

    //region PlanExtensionStrategy Implementation
    @Override
    public Iterable<P> extendPlan(Optional<P> plan, Q query) {
        List<P> plans = new LinkedList<>();
        for(PlanExtensionStrategy<P,Q> extensionStrategy : innerExtenders){
            extensionStrategy.extendPlan(plan, query).forEach(plans::add);
            if(plans.size() > 0) {
                break;
            }
        }
        return plans;
    }
    //endregion

    //region Fields
    protected Iterable<PlanExtensionStrategy<P,Q>> innerExtenders;
    //endregion
}
