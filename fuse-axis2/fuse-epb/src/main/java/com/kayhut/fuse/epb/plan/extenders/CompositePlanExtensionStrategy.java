package com.kayhut.fuse.epb.plan.extenders;

import com.codahale.metrics.Slf4jReporter;
import com.google.inject.Inject;
import com.kayhut.fuse.dispatcher.utils.LoggerAnnotation;
import com.kayhut.fuse.epb.plan.PlanExtensionStrategy;
import com.kayhut.fuse.model.asgQuery.IQuery;
import com.kayhut.fuse.model.execution.plan.IPlan;
import javaslang.collection.Stream;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

/**
 * Created by moti on 2/27/2017.
 */
public class CompositePlanExtensionStrategy<P extends IPlan, Q extends IQuery> implements PlanExtensionStrategy<P , Q> {
    //region Constructors
    @Inject
    @SafeVarargs
    public CompositePlanExtensionStrategy(PlanExtensionStrategy<P, Q> ... innerExtenders) {
        this(Stream.of(innerExtenders));
    }

    public CompositePlanExtensionStrategy(Iterable<PlanExtensionStrategy<P, Q>> innerExtenders) {
        this.innerExtenders = Stream.ofAll(innerExtenders).toJavaList();
    }
    //endregion

    //region PlanExtensionStrategy Implementation
    @Override
    public Iterable<P> extendPlan(Optional<P> plan, Q query) {
        List<P> plans = new LinkedList<>();
        for(PlanExtensionStrategy<P,Q> extensionStrategy : innerExtenders){
            extensionStrategy.extendPlan(plan, query).forEach(plans::add);
        }
        return plans;
    }
    //endregion

    //region Fields
    private Iterable<PlanExtensionStrategy<P,Q>> innerExtenders;
    //endregion
}
