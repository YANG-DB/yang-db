package com.kayhut.fuse.epb;

import com.google.inject.Binder;
import com.google.inject.TypeLiteral;
import com.google.inject.name.Names;
import com.kayhut.fuse.dispatcher.ModuleBase;
import com.kayhut.fuse.epb.plan.*;
import com.kayhut.fuse.epb.plan.cost.DummyPlanCostEstimator;
import com.kayhut.fuse.epb.plan.cost.DummyPlanOpCostEstimator;
import com.kayhut.fuse.epb.plan.extenders.AllDirectionsPlanExtensionStrategy;
import com.kayhut.fuse.epb.plan.extenders.CompositePlanExtensionStrategy;
import com.kayhut.fuse.epb.plan.extenders.InitialPlanGeneratorExtensionStrategy;
import com.kayhut.fuse.epb.plan.validation.SiblingOnlyPlanValidator;
import com.kayhut.fuse.model.asgQuery.AsgQuery;
import com.kayhut.fuse.model.execution.plan.Plan;
import com.kayhut.fuse.model.execution.plan.costs.SingleCost;
import com.typesafe.config.Config;
import org.jooby.Env;
import org.jooby.Jooby;

/**
 * Created by lior on 22/02/2017.
 */
public class EpbModule extends ModuleBase {

    @Override
    public void configureInner(Env env, Config conf, Binder binder) throws Throwable {
        binder.bind(SimpleEpbDriver.class).asEagerSingleton();
        binder.bind(new TypeLiteral<PlanSearcher<Plan<SingleCost>, AsgQuery>>(){}).to(new TypeLiteral<BottomUpPlanBuilderImpl<Plan<SingleCost>, AsgQuery>>(){}).asEagerSingleton();
        DummyPlanOpCostEstimator planOpCostEstimator = new DummyPlanOpCostEstimator();
        DummyPlanCostEstimator planCostEstimator = new DummyPlanCostEstimator();
        binder.bind(new TypeLiteral<PlanExtensionStrategy<Plan<SingleCost>, AsgQuery>>(){}).toInstance(new CompositePlanExtensionStrategy<>(new InitialPlanGeneratorExtensionStrategy<>(planOpCostEstimator, planCostEstimator), new AllDirectionsPlanExtensionStrategy<>(planOpCostEstimator, planCostEstimator)));
        binder.bind(new TypeLiteral<PlanPruneStrategy<Plan<SingleCost>>>(){}).annotatedWith(Names.named("GlobalPruningStrategy")).toInstance(new NoPruningPruneStrategy<>());
        binder.bind(new TypeLiteral<PlanPruneStrategy<Plan<SingleCost>>>(){}).annotatedWith(Names.named("LocalPruningStrategy")).toInstance(new NoPruningPruneStrategy<>());
        binder.bind(new TypeLiteral<PlanValidator<Plan<SingleCost>, AsgQuery>>(){}).toInstance(new SiblingOnlyPlanValidator<>());

    }
}
