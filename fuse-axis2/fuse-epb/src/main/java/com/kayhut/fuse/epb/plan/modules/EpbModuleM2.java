package com.kayhut.fuse.epb.plan.modules;

import com.google.inject.Binder;
import com.google.inject.PrivateModule;
import com.google.inject.TypeLiteral;
import com.kayhut.fuse.dispatcher.epb.*;
import com.kayhut.fuse.dispatcher.modules.ModuleBase;
import com.kayhut.fuse.epb.plan.BottomUpPlanSearcher;
import com.kayhut.fuse.epb.plan.estimation.CostEstimationConfig;
import com.kayhut.fuse.epb.plan.estimation.IncrementalEstimationContext;
import com.kayhut.fuse.epb.plan.estimation.dummy.DummyCostEstimator;
import com.kayhut.fuse.epb.plan.estimation.pattern.RegexPatternCostEstimator;
import com.kayhut.fuse.epb.plan.estimation.pattern.estimators.M1PatternCostEstimator;
import com.kayhut.fuse.epb.plan.estimation.pattern.estimators.M2PatternCostEstimator;
import com.kayhut.fuse.epb.plan.estimation.pattern.estimators.PatternCostEstimator;
import com.kayhut.fuse.epb.plan.extenders.M1.M1PlanExtensionStrategy;
import com.kayhut.fuse.epb.plan.extenders.M2.M2PlanExtensionStrategy;
import com.kayhut.fuse.epb.plan.pruners.M2GlobalPruner;
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
import com.kayhut.fuse.epb.plan.validation.M2PlanValidator;
import com.kayhut.fuse.model.asgQuery.AsgQuery;
import com.kayhut.fuse.model.execution.plan.PlanWithCost;
import com.kayhut.fuse.model.execution.plan.composite.Plan;
import com.kayhut.fuse.model.execution.plan.costs.CountEstimatesCost;
import com.kayhut.fuse.model.execution.plan.costs.PlanDetailedCost;
import com.typesafe.config.Config;
import org.jooby.Env;
import org.jooby.scope.RequestScoped;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.google.inject.name.Names.named;

/**
 *
 */
public class EpbModuleM2 extends ModuleBase {
    //region ModuleBase Implementation
    @Override
    public void configureInner(Env env, Config conf, Binder binder) throws Throwable {
        bindPlanSearcher(env, conf, binder);
        bindPlanExtensionStrategy(env, conf, binder);
        bindPlanValidator(env, conf, binder);
        bindCostEstimator(env, conf, binder);
        bindPlanPruneStrategy(env, conf, binder);
        bindPlanSelector(env, conf, binder);

        binder.bind(new TypeLiteral<PlanTracer.Builder<Plan, PlanDetailedCost>>(){}).in(RequestScoped.class);
    }
    //endregion

    //region Private Methods
    private void bindPlanSearcher(Env env, Config conf, Binder binder) {
        binder.install(new PrivateModule() {
            @Override
            protected void configure() {
                this.bind(new TypeLiteral<PlanSearcher<Plan, PlanDetailedCost, AsgQuery>>(){})
                        .annotatedWith(named(LoggingPlanSearcher.planSearcherParameter))
                        .to(new TypeLiteral<BottomUpPlanSearcher<Plan, PlanDetailedCost, AsgQuery>>(){});
                this.bind(Logger.class)
                        .annotatedWith(named(LoggingPlanSearcher.loggerParameter))
                        .toInstance(LoggerFactory.getLogger(BottomUpPlanSearcher.class));
                this.bind(new TypeLiteral<PlanSearcher<Plan, PlanDetailedCost, AsgQuery>>(){})
                        .annotatedWith(named(PlanTracer.Searcher.Provider.planSearcherParameter))
                        .to(new TypeLiteral<LoggingPlanSearcher<Plan, PlanDetailedCost, AsgQuery>>(){});
                this.bindConstant().annotatedWith(named(PlanTracer.Searcher.Provider.planSearcherNameParameter)).to(BottomUpPlanSearcher.class.getSimpleName());
                this.bind(new TypeLiteral<PlanSearcher<Plan, PlanDetailedCost, AsgQuery>>(){})
                        .toProvider(new TypeLiteral<PlanTracer.Searcher.Provider<Plan, PlanDetailedCost, AsgQuery>>(){});

                this.expose(new TypeLiteral<PlanSearcher<Plan, PlanDetailedCost, AsgQuery>>(){});
            }
        });
    }

    private void bindPlanExtensionStrategy(Env env, Config conf, Binder binder) {
        binder.install(new PrivateModule() {
            @Override
            protected void configure() {
                this.bind(new TypeLiteral<PlanExtensionStrategy<Plan, AsgQuery>>(){})
                        .annotatedWith(named(PlanTracer.ExtensionStrategy.Provider.planExtensionStrategyParameter))
                        .to(M2PlanExtensionStrategy.class).asEagerSingleton();
                this.bindConstant().annotatedWith(named(PlanTracer.ExtensionStrategy.Provider.planExtensionStrategyNameParameter)).to(M2PlanExtensionStrategy.class.getSimpleName());
                this.bind(new TypeLiteral<PlanExtensionStrategy<Plan, AsgQuery>>(){})
                        .toProvider(new TypeLiteral<PlanTracer.ExtensionStrategy.Provider<Plan, PlanDetailedCost, AsgQuery>>(){});

                this.expose(new TypeLiteral<PlanExtensionStrategy<Plan, AsgQuery>>(){});
            }
        });
    }

    private void bindPlanValidator(Env env, Config conf, Binder binder) {
        binder.install(new PrivateModule() {
            @Override
            protected void configure() {
                this.bind(new TypeLiteral<PlanValidator<Plan, AsgQuery>>(){})
                        .annotatedWith(named(PlanTracer.Validator.Provider.planValidatorParameter))
                        .to(M2PlanValidator.class).asEagerSingleton();
                this.bindConstant().annotatedWith(named(PlanTracer.Validator.Provider.planValidatorNameParameter)).to(M2PlanValidator.class.getSimpleName());
                this.bind(new TypeLiteral<PlanValidator<Plan, AsgQuery>>(){})
                        .toProvider(new TypeLiteral<PlanTracer.Validator.Provider<Plan, PlanDetailedCost, AsgQuery>>(){});

                this.expose(new TypeLiteral<PlanValidator<Plan, AsgQuery>>(){});
            }
        });
    }

    private void bindCostEstimator(Env env, Config conf, Binder binder) {
        binder.install(new PrivateModule() {
            @Override
            protected void configure() {
                this.bind(StatConfig.class).toInstance(new StatConfig(conf));
                this.bind(GraphStatisticsProvider.class).to(ElasticStatisticsGraphProvider.class).asEagerSingleton();
                this.bind(StatDataProvider.class).to(ElasticStatDocumentProvider.class).asEagerSingleton();

                this.bind(StatisticsProviderFactory.class).to(EBaseStatisticsProviderFactory.class).asEagerSingleton();

                this.bind(CostEstimationConfig.class)
                        .toInstance(new CostEstimationConfig(conf.getDouble("epb.cost.alpha"), conf.getDouble("epb.cost.delta")));
                this.bind(new TypeLiteral<PatternCostEstimator<Plan, CountEstimatesCost, IncrementalEstimationContext<Plan, PlanDetailedCost, AsgQuery>>>(){})
                        .to(M2PatternCostEstimator.class).asEagerSingleton();

                this.bind(new TypeLiteral<CostEstimator<Plan, PlanDetailedCost, IncrementalEstimationContext<Plan, PlanDetailedCost, AsgQuery>>>(){})
                        .annotatedWith(named(PlanTracer.Estimator.Provider.costEstimatorParameter))
                        .to(RegexPatternCostEstimator.class).asEagerSingleton();
                this.bindConstant().annotatedWith(named(PlanTracer.Estimator.Provider.costEstimatorNameParameter)).to(RegexPatternCostEstimator.class.getSimpleName());
                this.bind(new TypeLiteral<CostEstimator<Plan, PlanDetailedCost, IncrementalEstimationContext<Plan, PlanDetailedCost, AsgQuery>>>(){})
                        .toProvider(new TypeLiteral<PlanTracer.Estimator.Provider<Plan, PlanDetailedCost, IncrementalEstimationContext<Plan, PlanDetailedCost, AsgQuery>>>(){});

                this.expose(new TypeLiteral<CostEstimator<Plan, PlanDetailedCost, IncrementalEstimationContext<Plan, PlanDetailedCost, AsgQuery>>>(){});
            }
        });
    }

    private void bindPlanPruneStrategy(Env env, Config conf, Binder binder) {
        binder.install(new PrivateModule() {
            @Override
            protected void configure() {
                this.bind(new TypeLiteral<PlanPruneStrategy<PlanWithCost<Plan, PlanDetailedCost>>>(){})
                        .annotatedWith(named(PlanTracer.PruneStrategy.Provider.planPruneStrategyParameter))
                        .toInstance(new NoPruningPruneStrategy<>());
                this.bindConstant().annotatedWith(named(PlanTracer.PruneStrategy.Provider.planPruneStrategyNameParameter)).to("Local:" + NoPruningPruneStrategy.class.getSimpleName());
                this.bind(new TypeLiteral<PlanPruneStrategy<PlanWithCost<Plan, PlanDetailedCost>>>(){})
                        .annotatedWith(named(BottomUpPlanSearcher.localPruneStrategyParameter))
                        .toProvider(new TypeLiteral<PlanTracer.PruneStrategy.Provider<Plan, PlanDetailedCost>>(){});

                this.expose(new TypeLiteral<PlanPruneStrategy<PlanWithCost<Plan, PlanDetailedCost>>>(){})
                        .annotatedWith(named(BottomUpPlanSearcher.localPruneStrategyParameter));
            }
        });
        binder.install(new PrivateModule() {
            @Override
            protected void configure() {
                this.bind(new TypeLiteral<PlanPruneStrategy<PlanWithCost<Plan, PlanDetailedCost>>>(){})
                        .annotatedWith(named(PlanTracer.PruneStrategy.Provider.planPruneStrategyParameter))
                        .toInstance(new M2GlobalPruner());
                this.bindConstant().annotatedWith(named(PlanTracer.PruneStrategy.Provider.planPruneStrategyNameParameter)).to("Global:" + M2GlobalPruner.class.getSimpleName());
                this.bind(new TypeLiteral<PlanPruneStrategy<PlanWithCost<Plan, PlanDetailedCost>>>(){})
                        .annotatedWith(named(BottomUpPlanSearcher.globalPruneStrategyParameter))
                        .toProvider(new TypeLiteral<PlanTracer.PruneStrategy.Provider<Plan, PlanDetailedCost>>(){});

                this.expose(new TypeLiteral<PlanPruneStrategy<PlanWithCost<Plan, PlanDetailedCost>>>(){})
                        .annotatedWith(named(BottomUpPlanSearcher.globalPruneStrategyParameter));
            }
        });
    }

    private void bindPlanSelector(Env env, Config conf, Binder binder) {
        binder.install(new PrivateModule() {
            @Override
            protected void configure() {
                this.bind(new TypeLiteral<PlanSelector<PlanWithCost<Plan, PlanDetailedCost>, AsgQuery>>(){})
                        .annotatedWith(named(PlanTracer.Selector.Provider.planSelectorParameter))
                        .toInstance(new AllCompletePlanSelector<>());
                this.bindConstant().annotatedWith(named(PlanTracer.Selector.Provider.planSelectorNameParameter)).to("Local:" + AllCompletePlanSelector.class.getSimpleName());
                this.bind(new TypeLiteral<PlanSelector<PlanWithCost<Plan, PlanDetailedCost>, AsgQuery>>(){})
                        .annotatedWith(named(BottomUpPlanSearcher.localPlanSelectorParameter))
                        .toProvider(new TypeLiteral<PlanTracer.Selector.Provider<Plan, PlanDetailedCost, AsgQuery>>(){});

                this.expose(new TypeLiteral<PlanSelector<PlanWithCost<Plan, PlanDetailedCost>, AsgQuery>>(){})
                        .annotatedWith(named(BottomUpPlanSearcher.localPlanSelectorParameter));
            }
        });
        binder.install(new PrivateModule() {
            @Override
            protected void configure() {
                this.bind(new TypeLiteral<PlanSelector<PlanWithCost<Plan, PlanDetailedCost>, AsgQuery>>(){})
                        .annotatedWith(named(PlanTracer.Selector.Provider.planSelectorParameter))
                        .toInstance(new CheapestPlanSelector());
                this.bindConstant().annotatedWith(named(PlanTracer.Selector.Provider.planSelectorNameParameter)).to("Global:" + CheapestPlanSelector.class.getSimpleName());
                this.bind(new TypeLiteral<PlanSelector<PlanWithCost<Plan, PlanDetailedCost>, AsgQuery>>(){})
                        .annotatedWith(named(BottomUpPlanSearcher.globalPlanSelectorParameter))
                        .toProvider(new TypeLiteral<PlanTracer.Selector.Provider<Plan, PlanDetailedCost, AsgQuery>>(){});

                this.expose(new TypeLiteral<PlanSelector<PlanWithCost<Plan, PlanDetailedCost>, AsgQuery>>(){})
                        .annotatedWith(named(BottomUpPlanSearcher.globalPlanSelectorParameter));
            }
        });
    }
    //endregion
}
