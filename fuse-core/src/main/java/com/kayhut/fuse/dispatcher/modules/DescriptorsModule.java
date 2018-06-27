package com.kayhut.fuse.dispatcher.modules;

import com.google.inject.Binder;
import com.google.inject.TypeLiteral;
import com.kayhut.fuse.dispatcher.descriptors.*;
import com.kayhut.fuse.dispatcher.resource.CursorResource;
import com.kayhut.fuse.dispatcher.resource.PageResource;
import com.kayhut.fuse.dispatcher.resource.QueryResource;
import com.kayhut.fuse.model.asgQuery.AsgQuery;
import com.kayhut.fuse.model.descriptors.CompositeDescriptor;
import com.kayhut.fuse.model.descriptors.Descriptor;
import com.kayhut.fuse.model.descriptors.ToStringDescriptor;
import com.kayhut.fuse.model.execution.plan.PlanOp;
import com.kayhut.fuse.model.execution.plan.PlanWithCost;
import com.kayhut.fuse.model.execution.plan.composite.CompositePlanOp;
import com.kayhut.fuse.model.execution.plan.composite.Plan;
import com.kayhut.fuse.model.execution.plan.composite.descriptors.*;
import com.kayhut.fuse.model.execution.plan.costs.PlanDetailedCost;
import com.kayhut.fuse.model.execution.plan.descriptors.AsgQueryDescriptor;
import com.kayhut.fuse.model.execution.plan.descriptors.PlanWithCostDescriptor;
import com.kayhut.fuse.model.execution.plan.descriptors.QueryDescriptor;
import com.kayhut.fuse.model.execution.plan.entity.EntityJoinOp;
import com.kayhut.fuse.model.execution.plan.entity.EntityOp;
import com.kayhut.fuse.model.execution.plan.relation.RelationOp;
import com.kayhut.fuse.model.query.Query;
import com.typesafe.config.Config;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.jooby.Env;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by roman.margolis on 29/11/2017.
 */
public class DescriptorsModule extends ModuleBase {
    //region ModuleBase Implementation
    @Override
    protected void configureInner(Env env, Config config, Binder binder) throws Throwable {
        binder.bind(new TypeLiteral<Descriptor<Query>>(){}).to(QueryDescriptor.class).asEagerSingleton();
        binder.bind(new TypeLiteral<Descriptor<AsgQuery>>(){}).to(AsgQueryDescriptor.class).asEagerSingleton();

        binder.bind(new TypeLiteral<Descriptor<Iterable<PlanOp>>>(){}).toInstance(IterablePlanOpDescriptor.getFull());
        binder.bind(new TypeLiteral<Descriptor<CompositePlanOp>>(){}).to(CompositePlanOpDescriptor.class);

        binder.bind(new TypeLiteral<Descriptor<PlanWithCost<Plan, PlanDetailedCost>>>(){})
                .toInstance(new PlanWithCostDescriptor<>(
                        new CompositePlanOpDescriptor(getIterablePlanOpDescriptor(IterablePlanOpDescriptor.Mode.full)),
                        new ToStringDescriptor<>()));

        /*binder.bind(new TypeLiteral<Descriptor<PlanWithCost<Plan, PlanDetailedCost>>>(){})
                .toInstance(new PlanWithCostDescriptor<>(
                        new CompositePlanOpDescriptor(IterablePlanOpDescriptor.getFull()),
                        new ToStringDescriptor<>()));*/

        binder.bind(new TypeLiteral<Descriptor<GraphTraversal<?, ?>>>(){}).to(GraphTraversalDescriptor.class).asEagerSingleton();

        binder.bind(new TypeLiteral<Descriptor<QueryResource>>(){}).to(QueryResourceDescriptor.class).asEagerSingleton();
        binder.bind(new TypeLiteral<Descriptor<CursorResource>>(){}).to(CursorResourceDescriptor.class).asEagerSingleton();
        binder.bind(new TypeLiteral<Descriptor<PageResource>>(){}).to(PageResourceDescriptor.class).asEagerSingleton();
    }
    //endregion

    //Private Methods
    private IterablePlanOpDescriptor getIterablePlanOpDescriptor(IterablePlanOpDescriptor.Mode mode) {
        IterablePlanOpDescriptor iterablePlanOpDescriptor = new IterablePlanOpDescriptor(mode, null);

        Map<Class<?>, Descriptor<? extends PlanOp>> descriptors = new HashMap<>();
        descriptors.put(CompositePlanOp.class, new CompositePlanOpDescriptor(iterablePlanOpDescriptor));
        descriptors.put(EntityJoinOp.class, new EntityJoinOpDescriptor(iterablePlanOpDescriptor));
        descriptors.put(EntityOp.class, new EntityOpDescriptor());
        descriptors.put(RelationOp.class, new RelationOpDescriptor());

        iterablePlanOpDescriptor.setCompositeDescriptor(new CompositeDescriptor<>(descriptors, new ToStringDescriptor<>()));
        return iterablePlanOpDescriptor;
    }
    //endregion
}
