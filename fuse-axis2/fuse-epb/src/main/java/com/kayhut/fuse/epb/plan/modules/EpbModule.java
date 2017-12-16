package com.kayhut.fuse.epb.plan.modules;

import com.google.inject.Binder;
import com.google.inject.TypeLiteral;
import com.google.inject.name.Names;
import com.kayhut.fuse.dispatcher.epb.*;
import com.kayhut.fuse.dispatcher.modules.ModuleBase;
import com.kayhut.fuse.epb.plan.*;
import com.kayhut.fuse.epb.plan.estimation.CostEstimationConfig;
import com.kayhut.fuse.dispatcher.epb.CostEstimator;
import com.kayhut.fuse.epb.plan.estimation.pattern.RegexPatternCostEstimator;
import com.kayhut.fuse.epb.plan.estimation.pattern.estimators.PatternCostEstimator;
import com.kayhut.fuse.epb.plan.estimation.IncrementalEstimationContext;
import com.kayhut.fuse.epb.plan.estimation.pattern.estimators.M1PatternCostEstimator;
import com.kayhut.fuse.epb.plan.extenders.M1.M1PlanExtensionStrategy;
import com.kayhut.fuse.epb.plan.pruners.NoPruningPruneStrategy;
import com.kayhut.fuse.epb.plan.selectors.AllCompletePlanSelector;
import com.kayhut.fuse.epb.plan.selectors.CheapestPlanSelector;
import com.kayhut.fuse.epb.plan.statistics.EBaseStatisticsProviderFactory;
import com.kayhut.fuse.epb.plan.statistics.GraphStatisticsProvider;
import com.kayhut.fuse.epb.plan.statistics.StatisticsProviderFactory;
import com.kayhut.fuse.epb.plan.statistics.configuration.StatConfig;
import com.kayhut.fuse.epb.plan.statistics.provider.ElasticStatDocumentProvider;
import com.kayhut.fuse.epb.plan.statistics.provider.ElasticStatisticsGraphProvider;
import com.kayhut.fuse.epb.plan.statistics.provider.StatDataProvider;
import com.kayhut.fuse.epb.plan.validation.M1PlanValidator;
import com.kayhut.fuse.model.asgQuery.AsgQuery;
import com.kayhut.fuse.model.execution.plan.composite.Plan;
import com.kayhut.fuse.model.execution.plan.PlanWithCost;
import com.kayhut.fuse.model.execution.plan.costs.CountEstimatesCost;
import com.kayhut.fuse.model.execution.plan.costs.PlanDetailedCost;
import com.typesafe.config.Config;
import org.jooby.Env;

/**
 * Created by lior on 22/02/2017.
 */
public class EpbModule extends ModuleBase {

    @Override
    public void configureInner(Env env, Config conf, Binder binder) throws Throwable {
        binder.bind(new TypeLiteral<PlanSearcher<Plan, PlanDetailedCost, AsgQuery>>(){})
                .annotatedWith(Names.named(LoggingPlanSearcher.injectionName))
                .to(new TypeLiteral<BottomUpPlanSearcher<Plan, PlanDetailedCost, AsgQuery>>(){})
                .asEagerSingleton();
        binder.bind(new TypeLiteral<PlanSearcher<Plan, PlanDetailedCost, AsgQuery>>(){})
                .to(new TypeLiteral<LoggingPlanSearcher<Plan, PlanDetailedCost, AsgQuery>>(){})
                .asEagerSingleton();

        binder.bind(StatConfig.class).toInstance(new StatConfig(conf));
        binder.bind(GraphStatisticsProvider.class).to(ElasticStatisticsGraphProvider.class).asEagerSingleton();
        binder.bind(StatDataProvider.class).to(ElasticStatDocumentProvider.class).asEagerSingleton();

        binder.bind(StatisticsProviderFactory.class).to(EBaseStatisticsProviderFactory.class).asEagerSingleton();

        binder.bind(CostEstimationConfig.class)
                .toInstance(new CostEstimationConfig(conf.getDouble("epb.cost.alpha"), conf.getDouble("epb.cost.delta")));
        binder.bind(new TypeLiteral<PatternCostEstimator<Plan, CountEstimatesCost, IncrementalEstimationContext<Plan, PlanDetailedCost, AsgQuery>>>(){})
                .to(M1PatternCostEstimator.class).asEagerSingleton();

        binder.bind(new TypeLiteral<CostEstimator<Plan, PlanDetailedCost, IncrementalEstimationContext<Plan, PlanDetailedCost, AsgQuery>>>(){})
                .to(RegexPatternCostEstimator.class).asEagerSingleton();

        binder.bind(new TypeLiteral<PlanExtensionStrategy<Plan, AsgQuery>>(){}).to(M1PlanExtensionStrategy.class);

        binder.bind(new TypeLiteral<PlanPruneStrategy<PlanWithCost<Plan, PlanDetailedCost>>>(){})
                .annotatedWith(Names.named("GlobalPruningStrategy"))
                .toInstance(new NoPruningPruneStrategy<>());

        binder.bind(new TypeLiteral<PlanPruneStrategy<PlanWithCost<Plan, PlanDetailedCost>>>(){})
                .annotatedWith(Names.named("LocalPruningStrategy"))
                .toInstance(new NoPruningPruneStrategy<>());

        binder.bind(new TypeLiteral<PlanSelector<PlanWithCost<Plan, PlanDetailedCost>, AsgQuery>>(){})
                .annotatedWith(Names.named("GlobalPlanSelector"))
                .toInstance(new CheapestPlanSelector());

        binder.bind(new TypeLiteral<PlanSelector<PlanWithCost<Plan, PlanDetailedCost>, AsgQuery>>(){})
                .annotatedWith(Names.named("LocalPlanSelector"))
                .toInstance(new AllCompletePlanSelector<>());

        binder.bind(new TypeLiteral<PlanValidator<Plan, AsgQuery>>(){}).to(M1PlanValidator.class).asEagerSingleton();


    }
}
