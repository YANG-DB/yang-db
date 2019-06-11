package com.kayhut.fuse.epb.plan.modules;

/*-
 * #%L
 * fuse-dv-epb
 * %%
 * Copyright (C) 2016 - 2018 yangdb   ------ www.yangdb.org ------
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
import com.kayhut.fuse.dispatcher.epb.CostEstimator;
import com.kayhut.fuse.dispatcher.epb.PlanExtensionStrategy;
import com.kayhut.fuse.dispatcher.epb.PlanPruneStrategy;
import com.kayhut.fuse.dispatcher.epb.PlanTracer;
import com.kayhut.fuse.epb.plan.estimation.CostEstimationConfig;
import com.kayhut.fuse.epb.plan.estimation.IncrementalEstimationContext;
import com.kayhut.fuse.epb.plan.estimation.pattern.PredicateCostEstimator;
import com.kayhut.fuse.epb.plan.estimation.pattern.RegexPatternCostEstimator;
import com.kayhut.fuse.epb.plan.estimation.pattern.estimators.M1PatternCostEstimator;
import com.kayhut.fuse.epb.plan.estimation.pattern.estimators.PatternCostEstimator;
import com.kayhut.fuse.epb.plan.pruners.CheapestPlanPruneStrategy;
import com.kayhut.fuse.epb.plan.statistics.StatisticsProviderFactory;
import com.kayhut.fuse.model.asgQuery.AsgQuery;
import com.kayhut.fuse.model.execution.plan.PlanWithCost;
import com.kayhut.fuse.model.execution.plan.composite.Plan;
import com.kayhut.fuse.model.execution.plan.costs.CountEstimatesCost;
import com.kayhut.fuse.model.execution.plan.costs.PlanDetailedCost;
import com.typesafe.config.Config;
import org.jooby.Env;

import java.util.function.Predicate;

import static com.google.inject.name.Names.named;

public class EpbDfsRuleBasedRedundantModule extends BaseEpbModule {
    //region Private Methods
    @Override
    protected Class<? extends PlanExtensionStrategy<Plan, AsgQuery>> planExtensionStrategy(Config conf) throws ClassNotFoundException {
        return (Class<? extends PlanExtensionStrategy<Plan, AsgQuery>>) Class.forName(conf.getString(conf.getString("assembly") + ".plan_extension_strategy_class"));
    }

    @Override
    protected void bindCostEstimator(Env env, Config conf, Binder binder) {
        binder.install(new PrivateModule() {
            @Override
            protected void configure() {
                try {
                    this.bind(CostEstimationConfig.class)
                            .toInstance(new CostEstimationConfig(conf.getDouble("epb.cost.alpha"), conf.getDouble("epb.cost.delta")));
                    this.bind(new TypeLiteral<PatternCostEstimator<Plan, CountEstimatesCost, IncrementalEstimationContext<Plan, PlanDetailedCost, AsgQuery>>>() {})
                            .to(M1PatternCostEstimator.class).asEagerSingleton();
                    this.bind(StatisticsProviderFactory.class).to(getStatisticsProviderFactory(conf)).asEagerSingleton();

                    this.bind(new TypeLiteral<Predicate<Plan>>() {})
                            .annotatedWith(named(PredicateCostEstimator.planPredicateParameter))
                            .toInstance(plan -> plan.getOps().size() <= 2);
                    this.bind(new TypeLiteral<CostEstimator<Plan, PlanDetailedCost, IncrementalEstimationContext<Plan, PlanDetailedCost, AsgQuery>>>() {})
                            .annotatedWith(named(PredicateCostEstimator.trueCostEstimatorParameter))
                            .to(RegexPatternCostEstimator.class).asEagerSingleton();
                    this.bind(new TypeLiteral<CostEstimator<Plan, PlanDetailedCost, IncrementalEstimationContext<Plan, PlanDetailedCost, AsgQuery>>>() {})
                            .annotatedWith(named(PredicateCostEstimator.falseCostEstimatorParameter))
                            .toInstance((plan, context) -> new PlanWithCost<>(plan, context.getPreviousCost().get().getCost()));

                    this.bind(new TypeLiteral<CostEstimator<Plan, PlanDetailedCost, IncrementalEstimationContext<Plan, PlanDetailedCost, AsgQuery>>>() {})
                            .annotatedWith(named(PlanTracer.Estimator.Provider.costEstimatorParameter))
                            .to(new TypeLiteral<PredicateCostEstimator<Plan, PlanDetailedCost, IncrementalEstimationContext<Plan, PlanDetailedCost, AsgQuery>>>() {}).asEagerSingleton();
                    this.bindConstant().annotatedWith(named(PlanTracer.Estimator.Provider.costEstimatorNameParameter)).to(PredicateCostEstimator.class.getSimpleName());
                    this.bind(new TypeLiteral<CostEstimator<Plan, PlanDetailedCost, IncrementalEstimationContext<Plan, PlanDetailedCost, AsgQuery>>>() {})
                            .toProvider(new TypeLiteral<PlanTracer.Estimator.Provider<Plan, PlanDetailedCost, IncrementalEstimationContext<Plan, PlanDetailedCost, AsgQuery>>>() {});

                    this.expose(StatisticsProviderFactory.class);
                    this.expose(new TypeLiteral<CostEstimator<Plan, PlanDetailedCost, IncrementalEstimationContext<Plan, PlanDetailedCost, AsgQuery>>>() {});
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    protected PlanPruneStrategy<PlanWithCost<Plan,PlanDetailedCost>> globalPrunerStrategy(Config config) {
        return new CheapestPlanPruneStrategy();
    }


    protected Class<? extends StatisticsProviderFactory> getStatisticsProviderFactory(Config conf) throws ClassNotFoundException {
        return (Class<? extends StatisticsProviderFactory>) Class.forName(conf.getString(conf.getString("assembly") + ".stats_provider_class"));
    }

//endregion
}
