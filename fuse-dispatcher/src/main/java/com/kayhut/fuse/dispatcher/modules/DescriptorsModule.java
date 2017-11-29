package com.kayhut.fuse.dispatcher.modules;

import com.google.inject.Binder;
import com.google.inject.TypeLiteral;
import com.kayhut.fuse.dispatcher.context.CursorCreationOperationContext;
import com.kayhut.fuse.dispatcher.context.PageCreationOperationContext;
import com.kayhut.fuse.dispatcher.context.QueryCreationOperationContext;
import com.kayhut.fuse.dispatcher.context.QueryValidationOperationContext;
import com.kayhut.fuse.dispatcher.descriptors.*;
import com.kayhut.fuse.dispatcher.resource.CursorResource;
import com.kayhut.fuse.dispatcher.resource.PageResource;
import com.kayhut.fuse.dispatcher.resource.QueryResource;
import com.kayhut.fuse.model.asgQuery.AsgQuery;
import com.kayhut.fuse.model.execution.plan.PlanWithCost;
import com.kayhut.fuse.model.execution.plan.composite.Plan;
import com.kayhut.fuse.model.execution.plan.costs.PlanDetailedCost;
import com.typesafe.config.Config;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.jooby.Env;

/**
 * Created by roman.margolis on 29/11/2017.
 */
public class DescriptorsModule extends ModuleBase {
    //region ModuleBase Implementation
    @Override
    protected void configureInner(Env env, Config config, Binder binder) throws Throwable {
        binder.bind(new TypeLiteral<Descriptor<AsgQuery>>(){}).to(AsgQueryDescriptor.class).asEagerSingleton();
        binder.bind(new TypeLiteral<Descriptor<Plan>>(){}).to(PlanDescriptor.class).asEagerSingleton();
        binder.bind(new TypeLiteral<Descriptor<PlanWithCost<Plan, PlanDetailedCost>>>(){}).to(PlanWithCostDescriptor.class).asEagerSingleton();

        binder.bind(new TypeLiteral<Descriptor<GraphTraversal<?, ?>>>(){}).to(GraphTraversalDescriptor.class).asEagerSingleton();

        binder.bind(new TypeLiteral<Descriptor<QueryResource>>(){}).to(QueryResourceDescriptor.class).asEagerSingleton();
        binder.bind(new TypeLiteral<Descriptor<CursorResource>>(){}).to(CursorResourceDescriptor.class).asEagerSingleton();
        binder.bind(new TypeLiteral<Descriptor<PageResource>>(){}).to(PageResourceDescriptor.class).asEagerSingleton();

        binder.bind(new TypeLiteral<Descriptor<QueryCreationOperationContext>>(){}).to(QueryCreationOperationContextDescriptor.class).asEagerSingleton();
        binder.bind(new TypeLiteral<Descriptor<QueryValidationOperationContext>>(){}).to(QueryValidationOperationContextDescriptor.class).asEagerSingleton();
        binder.bind(new TypeLiteral<Descriptor<CursorCreationOperationContext>>(){}).to(CursorCreationOperationContextDescriptor.class).asEagerSingleton();
        binder.bind(new TypeLiteral<Descriptor<PageCreationOperationContext>>(){}).to(PageCreationOperationContextDescriptor.class).asEagerSingleton();
    }
    //endregion
}
