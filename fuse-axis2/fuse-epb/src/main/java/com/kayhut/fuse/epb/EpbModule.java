package com.kayhut.fuse.epb;

import com.google.inject.Binder;
import com.google.inject.TypeLiteral;
import com.google.inject.name.Names;
import com.kayhut.fuse.epb.plan.*;
import com.kayhut.fuse.epb.plan.extenders.AllDirectionsPlanExtensionStrategy;
import com.kayhut.fuse.epb.plan.extenders.CompositePlanExtensionStrategy;
import com.kayhut.fuse.epb.plan.extenders.InitialPlanGeneratorExtensionStrategy;
import com.kayhut.fuse.epb.plan.validation.SiblingOnlyPlanValidator;
import com.kayhut.fuse.epb.plan.wrappers.SimpleWrapperFactory;
import com.kayhut.fuse.model.asgQuery.AsgQuery;
import com.kayhut.fuse.model.execution.plan.Plan;
import com.kayhut.fuse.model.execution.plan.costs.SingleCost;
import com.typesafe.config.Config;
import org.jooby.Env;
import org.jooby.Jooby;

/**
 * Created by lior on 22/02/2017.
 */
public class EpbModule implements Jooby.Module  {

    @Override
    public void configure(Env env, Config conf, Binder binder) throws Throwable {
        binder.bind(SimpleEpbDriver.class).asEagerSingleton();
        binder.bind(new TypeLiteral<PlanSearcher<Plan, AsgQuery, SingleCost>>(){}).to(new TypeLiteral<BottomUpPlanBuilderImpl<Plan, AsgQuery, SingleCost>>(){}).asEagerSingleton();
        binder.bind(new TypeLiteral<PlanExtensionStrategy<Plan, AsgQuery>>(){}).toInstance(new CompositePlanExtensionStrategy<>(new InitialPlanGeneratorExtensionStrategy(), new AllDirectionsPlanExtensionStrategy()));
        binder.bind(new TypeLiteral<PlanPruneStrategy<Plan, SingleCost>>(){}).annotatedWith(Names.named("GlobalPruningStrategy")).toInstance(new NoPruningPruneStrategy<>());
        binder.bind(new TypeLiteral<PlanPruneStrategy<Plan, SingleCost>>(){}).annotatedWith(Names.named("LocalPruningStrategy")).toInstance(new NoPruningPruneStrategy<>());
        binder.bind(new TypeLiteral<PlanValidator<Plan, AsgQuery>>(){}).toInstance(new SiblingOnlyPlanValidator());
        binder.bind(new TypeLiteral<PlanWrapperFactory<Plan, AsgQuery, SingleCost>>(){}).toInstance(new SimpleWrapperFactory());

    }
}
