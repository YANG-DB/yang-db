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

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.google.inject.Binder;
import com.google.inject.PrivateModule;
import com.google.inject.TypeLiteral;
import com.typesafe.config.Config;
import com.yangdb.fuse.dispatcher.epb.CostEstimator;
import com.yangdb.fuse.dispatcher.epb.PlanExtensionStrategy;
import com.yangdb.fuse.dispatcher.epb.PlanPruneStrategy;
import com.yangdb.fuse.dispatcher.epb.PlanTracer;
import com.yangdb.fuse.epb.plan.estimation.IncrementalEstimationContext;
import com.yangdb.fuse.epb.plan.estimation.cache.CachedCostEstimator;
import com.yangdb.fuse.epb.plan.estimation.cache.EntityFilterOpDescriptor;
import com.yangdb.fuse.epb.plan.estimation.cache.EntityOpDescriptor;
import com.yangdb.fuse.epb.plan.estimation.count.CountCostEstimator;
import com.yangdb.fuse.epb.plan.estimation.pattern.PredicateCostEstimator;
import com.yangdb.fuse.epb.plan.pruners.CheapestPlanPruneStrategy;
import com.yangdb.fuse.epb.plan.statistics.StatisticsProviderFactory;
import com.yangdb.fuse.model.asgQuery.AsgQuery;
import com.yangdb.fuse.model.descriptors.CompositeDescriptor;
import com.yangdb.fuse.model.descriptors.Descriptor;
import com.yangdb.fuse.model.descriptors.ToStringDescriptor;
import com.yangdb.fuse.model.execution.plan.PlanOp;
import com.yangdb.fuse.model.execution.plan.PlanWithCost;
import com.yangdb.fuse.model.execution.plan.composite.Plan;
import com.yangdb.fuse.model.execution.plan.composite.descriptors.IterablePlanOpDescriptor;
import com.yangdb.fuse.model.execution.plan.costs.PlanDetailedCost;
import com.yangdb.fuse.model.execution.plan.entity.EntityFilterOp;
import com.yangdb.fuse.model.execution.plan.entity.EntityOp;
import org.jooby.Env;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;

import static com.google.inject.name.Names.named;

/**
 * Created by Roman on 3/14/2018.
 */
public class EpbDfsCountBasedRedundantModule  extends BaseEpbModule {
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
                IterablePlanOpDescriptor iterablePlanOpDescriptor = new IterablePlanOpDescriptor(IterablePlanOpDescriptor.Mode.full, null);
                Map<Class<?>, Descriptor<? extends PlanOp>> descriptors = new HashMap<>();
                descriptors.put(EntityOp.class, new EntityOpDescriptor());
                descriptors.put(EntityFilterOp.class, new EntityFilterOpDescriptor());
                iterablePlanOpDescriptor.setCompositeDescriptor(new CompositeDescriptor<>(descriptors, new ToStringDescriptor<>()));

                this.bind(new TypeLiteral<Descriptor<Plan>>() {})
                        .annotatedWith(named(CachedCostEstimator.descriptorParameter))
                        .toInstance((plan) -> iterablePlanOpDescriptor.describe(plan.getOps()));

                this.bind(new TypeLiteral<CostEstimator<Plan, PlanDetailedCost, IncrementalEstimationContext<Plan, PlanDetailedCost, AsgQuery>>>() {})
                        .annotatedWith(named(CachedCostEstimator.costEstimatorParameter))
                        .to(CountCostEstimator.class);
                this.bind(new TypeLiteral<Cache<String, PlanDetailedCost>>() {})
                        .annotatedWith(named(CachedCostEstimator.cacheParameter))
                        .toInstance(Caffeine.newBuilder()
                                .expireAfterAccess(Duration.ofMinutes(10))
                                .maximumSize(10000)
                                .build());

                this.bind(new TypeLiteral<Predicate<Plan>>() {})
                        .annotatedWith(named(PredicateCostEstimator.planPredicateParameter))
                        .toInstance(plan -> plan.getOps().size() <= 2);
                this.bind(new TypeLiteral<CostEstimator<Plan, PlanDetailedCost, IncrementalEstimationContext<Plan, PlanDetailedCost, AsgQuery>>>() {})
                        .annotatedWith(named(PredicateCostEstimator.trueCostEstimatorParameter))
                        .to(new TypeLiteral<CachedCostEstimator<Plan, PlanDetailedCost, IncrementalEstimationContext<Plan, PlanDetailedCost, AsgQuery>>>() {});
                this.bind(new TypeLiteral<CostEstimator<Plan, PlanDetailedCost, IncrementalEstimationContext<Plan, PlanDetailedCost, AsgQuery>>>() {})
                        .annotatedWith(named(PredicateCostEstimator.falseCostEstimatorParameter))
                        .toInstance((plan, context) -> new PlanWithCost<>(plan, context.getPreviousCost().get().getCost()));

                this.bind(new TypeLiteral<CostEstimator<Plan, PlanDetailedCost, IncrementalEstimationContext<Plan, PlanDetailedCost, AsgQuery>>>() {})
                        .annotatedWith(named(PlanTracer.Estimator.Provider.costEstimatorParameter))
                        .to(new TypeLiteral<PredicateCostEstimator<Plan, PlanDetailedCost, IncrementalEstimationContext<Plan, PlanDetailedCost, AsgQuery>>>() {});
                this.bindConstant().annotatedWith(named(PlanTracer.Estimator.Provider.costEstimatorNameParameter)).to(PredicateCostEstimator.class.getSimpleName());
                this.bind(new TypeLiteral<CostEstimator<Plan, PlanDetailedCost, IncrementalEstimationContext<Plan, PlanDetailedCost, AsgQuery>>>() {})
                        .toProvider(new TypeLiteral<PlanTracer.Estimator.Provider<Plan, PlanDetailedCost, IncrementalEstimationContext<Plan, PlanDetailedCost, AsgQuery>>>() {});

                try {
                    this.bind(StatisticsProviderFactory.class).to(getStatisticsProviderFactory(conf)).asEagerSingleton();
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }

                this.expose(StatisticsProviderFactory.class);
                this.expose(new TypeLiteral<CostEstimator<Plan, PlanDetailedCost, IncrementalEstimationContext<Plan, PlanDetailedCost, AsgQuery>>>() {});
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
