package com.yangdb.fuse.dispatcher.modules;

/*-
 * #%L
 * fuse-core
 * %%
 * Copyright (C) 2016 - 2019 The YangDb Graph Database Project
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */



import com.google.inject.Binder;
import com.google.inject.TypeLiteral;
import com.yangdb.fuse.dispatcher.descriptors.*;
import com.yangdb.fuse.dispatcher.resource.CursorResource;
import com.yangdb.fuse.dispatcher.resource.PageResource;
import com.yangdb.fuse.dispatcher.resource.QueryResource;
import com.yangdb.fuse.model.asgQuery.AsgQuery;
import com.yangdb.fuse.model.descriptors.CompositeDescriptor;
import com.yangdb.fuse.model.descriptors.Descriptor;
import com.yangdb.fuse.model.descriptors.ToStringDescriptor;
import com.yangdb.fuse.model.execution.plan.PlanOp;
import com.yangdb.fuse.model.execution.plan.PlanWithCost;
import com.yangdb.fuse.model.execution.plan.composite.CompositePlanOp;
import com.yangdb.fuse.model.execution.plan.composite.Plan;
import com.yangdb.fuse.model.execution.plan.composite.UnionOp;
import com.yangdb.fuse.model.execution.plan.composite.descriptors.*;
import com.yangdb.fuse.model.execution.plan.costs.PlanDetailedCost;
import com.yangdb.fuse.model.execution.plan.descriptors.AsgQueryDescriptor;
import com.yangdb.fuse.model.execution.plan.descriptors.PlanWithCostDescriptor;
import com.yangdb.fuse.model.execution.plan.descriptors.QueryDescriptor;
import com.yangdb.fuse.model.execution.plan.entity.EntityJoinOp;
import com.yangdb.fuse.model.execution.plan.entity.EntityOp;
import com.yangdb.fuse.model.execution.plan.relation.RelationOp;
import com.yangdb.fuse.model.query.Query;
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
        descriptors.put(UnionOp.class, new UnionOpDescriptor(iterablePlanOpDescriptor));
        descriptors.put(EntityOp.class, new EntityOpDescriptor());
        descriptors.put(RelationOp.class, new RelationOpDescriptor());

        iterablePlanOpDescriptor.setCompositeDescriptor(new CompositeDescriptor<>(descriptors, new ToStringDescriptor<>()));
        return iterablePlanOpDescriptor;
    }
    //endregion
}
