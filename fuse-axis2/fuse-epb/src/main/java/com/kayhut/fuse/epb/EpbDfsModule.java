package com.kayhut.fuse.epb;

import com.google.inject.Binder;
import com.google.inject.TypeLiteral;
import com.google.inject.name.Names;
import com.kayhut.fuse.dispatcher.ModuleBase;
import com.kayhut.fuse.epb.plan.*;
import com.kayhut.fuse.epb.plan.cost.CostEstimator;
import com.kayhut.fuse.epb.plan.cost.DummyCostEstimator;
import com.kayhut.fuse.epb.plan.extenders.CompositePlanExtensionStrategy;
import com.kayhut.fuse.epb.plan.extenders.dfs.StepDescendantAdjacentStrategy;
import com.kayhut.fuse.epb.plan.validation.SiblingOnlyPlanValidator;
import com.kayhut.fuse.model.asgQuery.AsgQuery;
import com.kayhut.fuse.model.execution.plan.Plan;
import com.kayhut.fuse.model.execution.plan.PlanWithCost;
import com.kayhut.fuse.model.execution.plan.costs.PlanDetailedCost;
import com.typesafe.config.Config;
import org.jooby.Env;

/**
 * Created by Roman on 24/04/2017.
 */
public class EpbDfsModule extends ModuleBase {

    @Override
    public void configureInner(Env env, Config conf, Binder binder) throws Throwable {
        binder.bind(SimpleEpbDriver.class).asEagerSingleton();
        binder.bind(new TypeLiteral<PlanSearcher<Plan, PlanDetailedCost, AsgQuery>>(){})
                .to(new TypeLiteral<BottomUpPlanBuilderImpl<Plan, PlanDetailedCost, AsgQuery>>(){}).asEagerSingleton();

        binder.bind(new TypeLiteral<CostEstimator<Plan, PlanDetailedCost>>(){})
                .toInstance(new DummyCostEstimator<>(new PlanDetailedCost()));

        binder.bind(new TypeLiteral<PlanExtensionStrategy<Plan, AsgQuery>>(){})
                .toInstance(
                        new CompositePlanExtensionStrategy<>(
                                new StepDescendantAdjacentStrategy()));

        binder.bind(new TypeLiteral<PlanPruneStrategy<PlanWithCost<Plan, PlanDetailedCost>>>(){})
                .annotatedWith(Names.named("GlobalPruningStrategy"))
                .toInstance(new NoPruningPruneStrategy<>());

        binder.bind(new TypeLiteral<PlanPruneStrategy<PlanWithCost<Plan, PlanDetailedCost>>>(){})
                .annotatedWith(Names.named("LocalPruningStrategy"))
                .toInstance(new NoPruningPruneStrategy<>());

        binder.bind(new TypeLiteral<PlanValidator<Plan, AsgQuery>>(){})
                .toInstance(new SiblingOnlyPlanValidator());

    }
}
