package com.kayhut.fuse.epb.plan.modules;

import com.google.inject.Binder;
import com.google.inject.TypeLiteral;
import com.google.inject.name.Names;
import com.kayhut.fuse.dispatcher.epb.*;
import com.kayhut.fuse.dispatcher.modules.ModuleBase;
import com.kayhut.fuse.epb.plan.*;
import com.kayhut.fuse.dispatcher.epb.CostEstimator;
import com.kayhut.fuse.epb.plan.estimation.dummy.DummyCostEstimator;
import com.kayhut.fuse.epb.plan.estimation.IncrementalEstimationContext;
import com.kayhut.fuse.epb.plan.extenders.M1.M1DfsRedundantPlanExtensionStrategy;
import com.kayhut.fuse.epb.plan.pruners.NoPruningPruneStrategy;
import com.kayhut.fuse.epb.plan.selectors.AllCompletePlanSelector;
import com.kayhut.fuse.epb.plan.validation.M1PlanValidator;
import com.kayhut.fuse.model.asgQuery.AsgQuery;
import com.kayhut.fuse.model.execution.plan.composite.Plan;
import com.kayhut.fuse.model.execution.plan.PlanWithCost;
import com.kayhut.fuse.model.execution.plan.costs.PlanDetailedCost;
import com.typesafe.config.Config;
import org.jooby.Env;
import org.jooby.scope.RequestScoped;

/**
 * Created by Roman on 22/05/2017.
 */
public class EpbDfsRedundantModule extends ModuleBase {

    @Override
    public void configureInner(Env env, Config conf, Binder binder) throws Throwable {
        binder.bind(new TypeLiteral<PlanSearcher<Plan, PlanDetailedCost, AsgQuery>>(){})
                .annotatedWith(Names.named(LoggingPlanSearcher.injectionName))
                .to(new TypeLiteral<BottomUpPlanSearcher<Plan, PlanDetailedCost, AsgQuery>>(){})
                .in(RequestScoped.class);
        binder.bind(new TypeLiteral<PlanSearcher<Plan, PlanDetailedCost, AsgQuery>>(){})
                .annotatedWith(Names.named(PlanTracer.Searcher.injectionName))
                .to(new TypeLiteral<LoggingPlanSearcher<Plan, PlanDetailedCost, AsgQuery>>(){})
                .in(RequestScoped.class);
        binder.bind(new TypeLiteral<PlanSearcher<Plan, PlanDetailedCost, AsgQuery>>(){})
                .to(new TypeLiteral<PlanTracer.Searcher<Plan, PlanDetailedCost, AsgQuery>>(){})
                .in(RequestScoped.class);


        binder.bind(new TypeLiteral<CostEstimator<Plan, PlanDetailedCost, IncrementalEstimationContext<Plan, PlanDetailedCost, AsgQuery>>>(){})
                .annotatedWith(Names.named(PlanTracer.Estimator.injectionName))
                .toInstance(new DummyCostEstimator<>(new PlanDetailedCost()));
        binder.bind(new TypeLiteral<CostEstimator<Plan, PlanDetailedCost, IncrementalEstimationContext<Plan, PlanDetailedCost, AsgQuery>>>(){})
                .to(new TypeLiteral<PlanTracer.Estimator<Plan, PlanDetailedCost, IncrementalEstimationContext<Plan, PlanDetailedCost, AsgQuery>>>(){})
                .in(RequestScoped.class);

        binder.bind(new TypeLiteral<PlanExtensionStrategy<Plan, AsgQuery>>(){})
                .annotatedWith(Names.named(PlanTracer.ExtensionStrategy.injectionName))
                .to(M1DfsRedundantPlanExtensionStrategy.class).asEagerSingleton();
        binder.bind(new TypeLiteral<PlanExtensionStrategy<Plan, AsgQuery>>(){})
                .to(new TypeLiteral<PlanTracer.ExtensionStrategy<Plan, AsgQuery>>(){})
                .in(RequestScoped.class);

        binder.bind(new TypeLiteral<PlanValidator<Plan, AsgQuery>>(){})
                .annotatedWith(Names.named(PlanTracer.Validator.injectionName))
                .to(M1PlanValidator.class)
                .asEagerSingleton();
        binder.bind(new TypeLiteral<PlanValidator<Plan, AsgQuery>>(){})
                .to(new TypeLiteral<PlanTracer.Validator<Plan, AsgQuery>>(){})
                .in(RequestScoped.class);

        binder.bind(new TypeLiteral<PlanPruneStrategy<PlanWithCost<Plan, PlanDetailedCost>>>(){})
                .annotatedWith(Names.named("GlobalPruningStrategy"))
                .toInstance(new NoPruningPruneStrategy<>());

        binder.bind(new TypeLiteral<PlanPruneStrategy<PlanWithCost<Plan, PlanDetailedCost>>>(){})
                .annotatedWith(Names.named("LocalPruningStrategy"))
                .toInstance(new NoPruningPruneStrategy<>());

        binder.bind(new TypeLiteral<PlanSelector<PlanWithCost<Plan, PlanDetailedCost>, AsgQuery>>(){})
                .annotatedWith(Names.named("GlobalPlanSelector"))
                .toInstance(new AllCompletePlanSelector<>());

        binder.bind(new TypeLiteral<PlanSelector<PlanWithCost<Plan, PlanDetailedCost>, AsgQuery>>(){})
                .annotatedWith(Names.named("LocalPlanSelector"))
                .toInstance(new AllCompletePlanSelector<>());

        binder.bind(PlanTracer.Builder.class).in(RequestScoped.class);
    }
}
