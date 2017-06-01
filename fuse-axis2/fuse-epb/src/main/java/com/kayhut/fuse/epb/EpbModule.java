package com.kayhut.fuse.epb;

import com.google.inject.Binder;
import com.google.inject.TypeLiteral;
import com.google.inject.name.Names;
import com.kayhut.fuse.dispatcher.ModuleBase;
import com.kayhut.fuse.epb.plan.*;
import com.kayhut.fuse.epb.plan.cost.CostEstimator;
import com.kayhut.fuse.epb.plan.cost.DummyCostEstimator;
import com.kayhut.fuse.epb.plan.extenders.AllDirectionsPlanExtensionStrategy;
import com.kayhut.fuse.epb.plan.extenders.CompositePlanExtensionStrategy;
import com.kayhut.fuse.epb.plan.extenders.InitialPlanGeneratorExtensionStrategy;
import com.kayhut.fuse.epb.plan.statistics.GraphStatisticsProvider;
import com.kayhut.fuse.epb.plan.statistics.configuration.StatConfig;
import com.kayhut.fuse.epb.plan.statistics.provider.ElasticStatisticsGraphProvider;
import com.kayhut.fuse.epb.plan.validation.M1PlanValidator;
import com.kayhut.fuse.model.asgQuery.AsgQuery;
import com.kayhut.fuse.model.execution.plan.Plan;
import com.kayhut.fuse.model.execution.plan.PlanWithCost;
import com.kayhut.fuse.model.execution.plan.costs.PlanDetailedCost;
import com.typesafe.config.Config;
import org.jooby.Env;

/**
 * Created by lior on 22/02/2017.
 */
public class EpbModule extends ModuleBase {

    @Override
    public void configureInner(Env env, Config conf, Binder binder) throws Throwable {
        binder.bind(SimpleEpbDriver.class).asEagerSingleton();
        binder.bind(new TypeLiteral<PlanSearcher<Plan, PlanDetailedCost, AsgQuery>>(){})
                .to(new TypeLiteral<BottomUpPlanSearcher<Plan, PlanDetailedCost, AsgQuery>>(){}).asEagerSingleton();

        binder.bind(new TypeLiteral<CostEstimator<Plan, PlanDetailedCost, AsgQuery>>(){})
                .toInstance(new DummyCostEstimator<>(new PlanDetailedCost()));

        binder.bind(new TypeLiteral<PlanExtensionStrategy<Plan, AsgQuery>>(){})
                .toInstance(
                        new CompositePlanExtensionStrategy<>(
                                new InitialPlanGeneratorExtensionStrategy(),
                                new AllDirectionsPlanExtensionStrategy()));

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

        binder.bind(new TypeLiteral<PlanValidator<Plan, AsgQuery>>(){}).to(M1PlanValidator.class).asEagerSingleton();

        binder.bind(StatConfig.class).toInstance(new StatConfig(conf));
        binder.bind(GraphStatisticsProvider.class).to(ElasticStatisticsGraphProvider.class).asEagerSingleton();

    }
}
