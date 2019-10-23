package com.yangdb.fuse.epb.plan.modules;

/*-
 * #%L
 * fuse-dv-epb
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
import com.google.inject.PrivateModule;
import com.google.inject.TypeLiteral;
import com.yangdb.fuse.dispatcher.epb.*;
import com.yangdb.fuse.dispatcher.modules.ModuleBase;
import com.yangdb.fuse.epb.plan.BottomUpPlanSearcher;
import com.yangdb.fuse.epb.plan.UnionPlanSearcher;
import com.yangdb.fuse.epb.plan.estimation.IncrementalEstimationContext;
import com.yangdb.fuse.epb.plan.estimation.dummy.DummyCostEstimator;
import com.yangdb.fuse.epb.plan.extenders.M1.M1DfsRedundantPlanExtensionStrategy;
import com.yangdb.fuse.epb.plan.pruners.NoPruningPruneStrategy;
import com.yangdb.fuse.epb.plan.selectors.AllCompletePlanSelector;
import com.yangdb.fuse.epb.plan.statistics.NoStatsProvider;
import com.yangdb.fuse.epb.plan.statistics.StatisticsProviderFactory;
import com.yangdb.fuse.epb.plan.validation.M1PlanValidator;
import com.yangdb.fuse.model.asgQuery.AsgQuery;
import com.yangdb.fuse.model.execution.plan.PlanWithCost;
import com.yangdb.fuse.model.execution.plan.composite.Plan;
import com.yangdb.fuse.model.execution.plan.costs.PlanDetailedCost;
import com.typesafe.config.Config;
import org.jooby.Env;
import org.jooby.scope.RequestScoped;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.google.inject.name.Names.named;

/**
 * Created by lior.perry on 2/18/2018.
 */
public abstract class BaseEpbModule extends ModuleBase {
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

    private void bindPlanSearcher(Env env, Config conf, Binder binder) {
        binder.install(new PrivateModule() {
            @Override
            protected void configure() {
                this.bind(new TypeLiteral<PlanSearcher<Plan, PlanDetailedCost, AsgQuery>>() {})
                        .annotatedWith(named(UnionPlanSearcher.planSearcherParameter))
                        .to(new TypeLiteral<BottomUpPlanSearcher<Plan, PlanDetailedCost, AsgQuery>>() {});
                this.bind(new TypeLiteral<PlanSearcher<Plan, PlanDetailedCost, AsgQuery>>() {})
                        .annotatedWith(named(LoggingPlanSearcher.planSearcherParameter))
                        .to(new TypeLiteral<UnionPlanSearcher>() {});
                this.bind(Logger.class)
                        .annotatedWith(named(LoggingPlanSearcher.loggerParameter))
                        .toInstance(LoggerFactory.getLogger(UnionPlanSearcher.class));
                this.bind(new TypeLiteral<PlanSearcher<Plan, PlanDetailedCost, AsgQuery>>() {})
                        .annotatedWith(named(PlanTracer.Searcher.Provider.planSearcherParameter))
                        .to(new TypeLiteral<LoggingPlanSearcher<Plan, PlanDetailedCost, AsgQuery>>() {});
                this.bindConstant().annotatedWith(named(PlanTracer.Searcher.Provider.planSearcherNameParameter)).to(UnionPlanSearcher.class.getSimpleName());
                this.bind(new TypeLiteral<PlanSearcher<Plan, PlanDetailedCost, AsgQuery>>() {})
                        .toProvider(new TypeLiteral<PlanTracer.Searcher.Provider<Plan, PlanDetailedCost, AsgQuery>>() {});

                this.expose(new TypeLiteral<PlanSearcher<Plan, PlanDetailedCost, AsgQuery>>() {});
            }
        });
    }

    protected void bindPlanExtensionStrategy(Env env, Config conf, Binder binder) {
        binder.install(new PrivateModule() {
            @Override
            protected void configure() {
                try {
                    this.bind(new TypeLiteral<PlanExtensionStrategy<Plan, AsgQuery>>() {})
                            .annotatedWith(named(PlanTracer.ExtensionStrategy.Provider.planExtensionStrategyParameter))
                            .to(planExtensionStrategy(conf));

                    this.bindConstant()
                            .annotatedWith(named(PlanTracer.ExtensionStrategy.Provider.planExtensionStrategyNameParameter))
                            .to(M1DfsRedundantPlanExtensionStrategy.class.getSimpleName());
                    this.bind(new TypeLiteral<PlanExtensionStrategy<Plan, AsgQuery>>() {})
                            .toProvider(new TypeLiteral<PlanTracer.ExtensionStrategy.Provider<Plan, PlanDetailedCost, AsgQuery>>() {});

                    this.expose(new TypeLiteral<PlanExtensionStrategy<Plan, AsgQuery>>() {});
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    protected void bindPlanSelector(Env env, Config conf, Binder binder) {
        binder.install(new PrivateModule() {
            @Override
            protected void configure() {
                this.bind(new TypeLiteral<PlanSelector<PlanWithCost<Plan, PlanDetailedCost>, AsgQuery>>() {})
                        .annotatedWith(named(PlanTracer.Selector.Provider.planSelectorParameter))
                        .toInstance(localPlanSelector(conf));
                this.bindConstant().annotatedWith(named(PlanTracer.Selector.Provider.planSelectorNameParameter)).to("Local:" + AllCompletePlanSelector.class.getSimpleName());
                this.bind(new TypeLiteral<PlanSelector<PlanWithCost<Plan, PlanDetailedCost>, AsgQuery>>() {})
                        .annotatedWith(named(BottomUpPlanSearcher.localPlanSelectorParameter))
                        .toProvider(new TypeLiteral<PlanTracer.Selector.Provider<Plan, PlanDetailedCost, AsgQuery>>() {});

                this.expose(new TypeLiteral<PlanSelector<PlanWithCost<Plan, PlanDetailedCost>, AsgQuery>>() {})
                        .annotatedWith(named(BottomUpPlanSearcher.localPlanSelectorParameter));
            }
        });
        binder.install(new PrivateModule() {
            @Override
            protected void configure() {
                this.bind(new TypeLiteral<PlanSelector<PlanWithCost<Plan, PlanDetailedCost>, AsgQuery>>() {})
                        .annotatedWith(named(PlanTracer.Selector.Provider.planSelectorParameter))
                        .toInstance(globalPlanSelector(conf));
                this.bindConstant().annotatedWith(named(PlanTracer.Selector.Provider.planSelectorNameParameter)).to("Global:" + AllCompletePlanSelector.class.getSimpleName());
                this.bind(new TypeLiteral<PlanSelector<PlanWithCost<Plan, PlanDetailedCost>, AsgQuery>>() {})
                        .annotatedWith(named(BottomUpPlanSearcher.globalPlanSelectorParameter))
                        .toProvider(new TypeLiteral<PlanTracer.Selector.Provider<Plan, PlanDetailedCost, AsgQuery>>() {});

                this.expose(new TypeLiteral<PlanSelector<PlanWithCost<Plan, PlanDetailedCost>, AsgQuery>>() {})
                        .annotatedWith(named(BottomUpPlanSearcher.globalPlanSelectorParameter));
            }
        });
    }

    protected PlanSelector<PlanWithCost<Plan, PlanDetailedCost>, AsgQuery> localPlanSelector(Config conf) {
        return new AllCompletePlanSelector<>();
    }

    protected PlanSelector<PlanWithCost<Plan, PlanDetailedCost>, AsgQuery> globalPlanSelector(Config conf) {
        return new AllCompletePlanSelector<>();
    }


    protected void bindPlanValidator(Env env, Config conf, Binder binder) {
        binder.install(new PrivateModule() {
            @Override
            protected void configure() {
                this.bind(new TypeLiteral<PlanValidator<Plan, AsgQuery>>() {})
                        .annotatedWith(named(PlanTracer.Validator.Provider.planValidatorParameter))
                        .to(planValidator());
                this.bindConstant().annotatedWith(named(PlanTracer.Validator.Provider.planValidatorNameParameter)).to(M1PlanValidator.class.getSimpleName());
                this.bind(new TypeLiteral<PlanValidator<Plan, AsgQuery>>() {})
                        .toProvider(new TypeLiteral<PlanTracer.Validator.Provider<Plan, PlanDetailedCost, AsgQuery>>() {});

                this.expose(new TypeLiteral<PlanValidator<Plan, AsgQuery>>() {});
            }
        });
    }

    protected Class<? extends PlanValidator<Plan, AsgQuery>> planValidator() {
        return M1PlanValidator.class;
    }

    protected void bindCostEstimator(Env env, Config conf, Binder binder) {
        binder.install(new PrivateModule() {
            @Override
            protected void configure() {
                this.bind(new TypeLiteral<CostEstimator<Plan, PlanDetailedCost, IncrementalEstimationContext<Plan, PlanDetailedCost, AsgQuery>>>() {})
                        .annotatedWith(named(PlanTracer.Estimator.Provider.costEstimatorParameter))
                        .toInstance(new DummyCostEstimator<>(new PlanDetailedCost()));
                this.bindConstant().annotatedWith(named(PlanTracer.Estimator.Provider.costEstimatorNameParameter)).to(DummyCostEstimator.class.getSimpleName());
                this.bind(new TypeLiteral<CostEstimator<Plan, PlanDetailedCost, IncrementalEstimationContext<Plan, PlanDetailedCost, AsgQuery>>>() {})
                        .toProvider(new TypeLiteral<PlanTracer.Estimator.Provider<Plan, PlanDetailedCost, IncrementalEstimationContext<Plan, PlanDetailedCost, AsgQuery>>>() {});
                this.bind(StatisticsProviderFactory.class).to(NoStatsProvider.class);


                this.expose(StatisticsProviderFactory.class);
                this.expose(new TypeLiteral<CostEstimator<Plan, PlanDetailedCost, IncrementalEstimationContext<Plan, PlanDetailedCost, AsgQuery>>>() {});
            }
        });
    }


    private void bindPlanPruneStrategy(Env env, Config conf, Binder binder) {
        localPruner(binder, conf);
        globalPruner(binder, conf);
    }

    protected void globalPruner(Binder binder, Config conf) {
        binder.install(new PrivateModule() {
            @Override
            protected void configure() {
                this.bind(new TypeLiteral<PlanPruneStrategy<PlanWithCost<Plan, PlanDetailedCost>>>() {})
                        .annotatedWith(named(PlanTracer.PruneStrategy.Provider.planPruneStrategyParameter))
                        .toInstance(globalPrunerStrategy(conf));
                this.bindConstant().annotatedWith(named(PlanTracer.PruneStrategy.Provider.planPruneStrategyNameParameter)).to("Global:" + NoPruningPruneStrategy.class.getSimpleName());
                this.bind(new TypeLiteral<PlanPruneStrategy<PlanWithCost<Plan, PlanDetailedCost>>>() {})
                        .annotatedWith(named(BottomUpPlanSearcher.globalPruneStrategyParameter))
                        .toProvider(new TypeLiteral<PlanTracer.PruneStrategy.Provider<Plan, PlanDetailedCost>>() {});

                this.expose(new TypeLiteral<PlanPruneStrategy<PlanWithCost<Plan, PlanDetailedCost>>>() {})
                        .annotatedWith(named(BottomUpPlanSearcher.globalPruneStrategyParameter));
            }
        });
    }

    protected void localPruner(Binder binder, Config conf) {
        binder.install(new PrivateModule() {
            @Override
            protected void configure() {
                this.bind(new TypeLiteral<PlanPruneStrategy<PlanWithCost<Plan, PlanDetailedCost>>>() {})
                        .annotatedWith(named(PlanTracer.PruneStrategy.Provider.planPruneStrategyParameter))
                        .toInstance(localPrunerStrategy(conf));
                this.bindConstant().annotatedWith(named(PlanTracer.PruneStrategy.Provider.planPruneStrategyNameParameter)).to("Local:" + NoPruningPruneStrategy.class.getSimpleName());
                this.bind(new TypeLiteral<PlanPruneStrategy<PlanWithCost<Plan, PlanDetailedCost>>>() {})
                        .annotatedWith(named(BottomUpPlanSearcher.localPruneStrategyParameter))
                        .toProvider(new TypeLiteral<PlanTracer.PruneStrategy.Provider<Plan, PlanDetailedCost>>() {});

                this.expose(new TypeLiteral<PlanPruneStrategy<PlanWithCost<Plan, PlanDetailedCost>>>() {})
                        .annotatedWith(named(BottomUpPlanSearcher.localPruneStrategyParameter));
            }
        });
    }

    protected PlanPruneStrategy<PlanWithCost<Plan, PlanDetailedCost>> localPrunerStrategy(Config conf) {
        return new NoPruningPruneStrategy<>();
    }

    protected PlanPruneStrategy<PlanWithCost<Plan, PlanDetailedCost>> globalPrunerStrategy(Config conf) {
        return new NoPruningPruneStrategy<>();
    }


    protected abstract Class<? extends PlanExtensionStrategy<Plan, AsgQuery>> planExtensionStrategy(Config conf) throws ClassNotFoundException;

    //endregion


}
