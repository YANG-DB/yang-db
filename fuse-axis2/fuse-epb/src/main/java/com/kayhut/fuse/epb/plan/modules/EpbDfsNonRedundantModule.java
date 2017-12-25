package com.kayhut.fuse.epb.plan.modules;

import com.google.inject.Binder;
import com.google.inject.PrivateModule;
import com.google.inject.TypeLiteral;
import com.google.inject.name.Names;
import com.kayhut.fuse.dispatcher.epb.*;
import com.kayhut.fuse.dispatcher.modules.ModuleBase;
import com.kayhut.fuse.epb.plan.*;
import com.kayhut.fuse.dispatcher.epb.CostEstimator;
import com.kayhut.fuse.epb.plan.estimation.dummy.DummyCostEstimator;
import com.kayhut.fuse.epb.plan.estimation.IncrementalEstimationContext;
import com.kayhut.fuse.epb.plan.extenders.M1.M1DfsNonRedundantPlanExtensionStrategy;
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

import static com.google.inject.name.Names.named;

/**
 * Created by Roman on 24/04/2017.
 */
public class EpbDfsNonRedundantModule extends ModuleBase {
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
                        .annotatedWith(named(LoggingPlanSearcher.injectionName))
                        .to(new TypeLiteral<BottomUpPlanSearcher<Plan, PlanDetailedCost, AsgQuery>>(){});
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
                        .to(M1DfsNonRedundantPlanExtensionStrategy.class).asEagerSingleton();
                this.bindConstant().annotatedWith(named(PlanTracer.ExtensionStrategy.Provider.planExtensionStrategyNameParameter)).to(M1DfsNonRedundantPlanExtensionStrategy.class.getSimpleName());
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
                        .to(M1PlanValidator.class).asEagerSingleton();
                this.bindConstant().annotatedWith(named(PlanTracer.Validator.Provider.planValidatorNameParameter)).to(M1PlanValidator.class.getSimpleName());
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
                this.bind(new TypeLiteral<CostEstimator<Plan, PlanDetailedCost, IncrementalEstimationContext<Plan, PlanDetailedCost, AsgQuery>>>(){})
                        .annotatedWith(named(PlanTracer.Estimator.Provider.costEstimatorParameter))
                        .toInstance(new DummyCostEstimator<>(new PlanDetailedCost()));
                this.bindConstant().annotatedWith(named(PlanTracer.Estimator.Provider.costEstimatorNameParameter)).to(DummyCostEstimator.class.getSimpleName());
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
                        .toInstance(new NoPruningPruneStrategy<>());
                this.bindConstant().annotatedWith(named(PlanTracer.PruneStrategy.Provider.planPruneStrategyNameParameter)).to("Global:" + NoPruningPruneStrategy.class.getSimpleName());
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
                        .toInstance(new AllCompletePlanSelector<>());
                this.bindConstant().annotatedWith(named(PlanTracer.Selector.Provider.planSelectorNameParameter)).to("Global:" + AllCompletePlanSelector.class.getSimpleName());
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
