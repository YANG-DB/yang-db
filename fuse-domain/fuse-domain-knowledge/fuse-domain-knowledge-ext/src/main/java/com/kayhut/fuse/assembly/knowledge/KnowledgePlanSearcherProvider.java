package com.kayhut.fuse.assembly.knowledge;

/*-
 * #%L
 * fuse-domain-knowledge-ext
 * %%
 * Copyright (C) 2016 - 2018 kayhut
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

import com.codahale.metrics.MetricRegistry;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.google.inject.Inject;
import com.kayhut.fuse.dispatcher.epb.CostEstimator;
import com.kayhut.fuse.dispatcher.epb.LoggingPlanSearcher;
import com.kayhut.fuse.dispatcher.epb.PlanSearcher;
import com.kayhut.fuse.dispatcher.gta.PlanTraversalTranslator;
import com.kayhut.fuse.dispatcher.ontology.OntologyProvider;
import com.kayhut.fuse.epb.plan.BottomUpPlanSearcher;
import com.kayhut.fuse.epb.plan.OptionalSplitPlanSearcher;
import com.kayhut.fuse.epb.plan.estimation.IncrementalEstimationContext;
import com.kayhut.fuse.epb.plan.estimation.cache.CachedCostEstimator;
import com.kayhut.fuse.epb.plan.estimation.cache.EntityFilterOpDescriptor;
import com.kayhut.fuse.epb.plan.estimation.cache.EntityOpDescriptor;
import com.kayhut.fuse.epb.plan.estimation.count.CountCostEstimator;
import com.kayhut.fuse.epb.plan.estimation.dummy.DummyCostEstimator;
import com.kayhut.fuse.epb.plan.estimation.pattern.PredicateCostEstimator;
import com.kayhut.fuse.epb.plan.extenders.M1.M1DfsRedundantPlanExtensionStrategy;
import com.kayhut.fuse.epb.plan.pruners.CheapestPlanPruneStrategy;
import com.kayhut.fuse.epb.plan.pruners.FirstPlanPruneStrategy;
import com.kayhut.fuse.epb.plan.pruners.NoPruningPruneStrategy;
import com.kayhut.fuse.epb.plan.selectors.AllCompletePlanSelector;
import com.kayhut.fuse.epb.plan.validation.M1PlanValidator;
import com.kayhut.fuse.executor.ontology.GraphElementSchemaProviderFactory;
import com.kayhut.fuse.executor.ontology.UniGraphProvider;
import com.kayhut.fuse.model.asgQuery.AsgQuery;
import com.kayhut.fuse.model.descriptors.CompositeDescriptor;
import com.kayhut.fuse.model.descriptors.Descriptor;
import com.kayhut.fuse.model.descriptors.ToStringDescriptor;
import com.kayhut.fuse.model.execution.plan.PlanOp;
import com.kayhut.fuse.model.execution.plan.PlanWithCost;
import com.kayhut.fuse.model.execution.plan.composite.Plan;
import com.kayhut.fuse.model.execution.plan.composite.descriptors.IterablePlanOpDescriptor;
import com.kayhut.fuse.model.execution.plan.costs.DoubleCost;
import com.kayhut.fuse.model.execution.plan.costs.PlanDetailedCost;
import com.kayhut.fuse.model.execution.plan.entity.EntityFilterOp;
import com.kayhut.fuse.model.execution.plan.entity.EntityOp;
import org.slf4j.LoggerFactory;

import javax.xml.ws.Provider;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Roman on 8/20/2018.
 */
public class KnowledgePlanSearcherProvider  implements com.google.inject.Provider<PlanSearcher<Plan, PlanDetailedCost, AsgQuery>> {
    //region Constructors
    @Inject
    public KnowledgePlanSearcherProvider(
            MetricRegistry metricRegistry,
            Descriptor<PlanWithCost<Plan, PlanDetailedCost>> planWithCostDescriptor,
            OntologyProvider ontologyProvider,
            GraphElementSchemaProviderFactory schemaProviderFactory,
            PlanTraversalTranslator planTraversalTranslator,
            UniGraphProvider uniGraphProvider) {
        this.metricRegistry = metricRegistry;
        this.planWithCostDescriptor = planWithCostDescriptor;
        this.ontologyProvider = ontologyProvider;
        this.schemaProviderFactory = schemaProviderFactory;
        this.planTraversalTranslator = planTraversalTranslator;
        this.uniGraphProvider = uniGraphProvider;
    }
    //endregion

    //region Provider Implementation
    @Override
    public PlanSearcher<Plan, PlanDetailedCost, AsgQuery> get() {
        return new LoggingPlanSearcher<>(
                new OptionalSplitPlanSearcher(
                        new BottomUpPlanSearcher<>(
                                new KnowledgeM2DfsRedundantPlanExtensionStrategy(
                                        this.ontologyProvider,
                                        this.schemaProviderFactory),
                                new CheapestPlanPruneStrategy(),
                                new NoPruningPruneStrategy<>(),
                                new AllCompletePlanSelector<>(),
                                new AllCompletePlanSelector<>(),
                                new M1PlanValidator(),
                                this.getCostEstimator()),
                        new BottomUpPlanSearcher<>(
                                new M1DfsRedundantPlanExtensionStrategy(this.ontologyProvider, this.schemaProviderFactory),
                                new FirstPlanPruneStrategy<>(),
                                new FirstPlanPruneStrategy<>(),
                                new AllCompletePlanSelector<>(),
                                new AllCompletePlanSelector<>(),
                                new M1PlanValidator(),
                                new DummyCostEstimator<>(new PlanDetailedCost(null, null)))),
                this.planWithCostDescriptor,
                LoggerFactory.getLogger(OptionalSplitPlanSearcher.class),
                this.metricRegistry);
    }
    //endregion

    //region Private Methods
    private CostEstimator<Plan, PlanDetailedCost, IncrementalEstimationContext<Plan, PlanDetailedCost, AsgQuery>> getCostEstimator() {
        if (costEstimator == null) {
            synchronized (sync) {
                if (costEstimator == null) {
                    IterablePlanOpDescriptor iterablePlanOpDescriptor = new IterablePlanOpDescriptor(IterablePlanOpDescriptor.Mode.full, null);
                    Map<Class<?>, Descriptor<? extends PlanOp>> descriptors = new HashMap<>();
                    descriptors.put(EntityOp.class, new EntityOpDescriptor());
                    descriptors.put(EntityFilterOp.class, new EntityFilterOpDescriptor());
                    iterablePlanOpDescriptor.setCompositeDescriptor(new CompositeDescriptor<>(descriptors, new ToStringDescriptor<>()));

                    costEstimator = new PredicateCostEstimator<>(
                            plan -> plan.getOps().size() <= 2,
                            new CachedCostEstimator<>(
                                    new CountCostEstimator(this.ontologyProvider, this.planTraversalTranslator, this.uniGraphProvider),
                                    Caffeine.newBuilder()
                                            .expireAfterAccess(Duration.ofMinutes(10))
                                            .maximumSize(10000)
                                            .build(),
                                    (plan) -> iterablePlanOpDescriptor.describe(plan.getOps())),
                            (plan, context) -> new PlanWithCost<>(plan, context.getPreviousCost().get().getCost()));
                }
            }
        }

        return costEstimator;
    }
    //endregion

    //region Fields
    private MetricRegistry metricRegistry;
    private Descriptor<PlanWithCost<Plan, PlanDetailedCost>> planWithCostDescriptor;
    private OntologyProvider ontologyProvider;
    private GraphElementSchemaProviderFactory schemaProviderFactory;
    private PlanTraversalTranslator planTraversalTranslator;
    private UniGraphProvider uniGraphProvider;

    private static CostEstimator<Plan, PlanDetailedCost, IncrementalEstimationContext<Plan, PlanDetailedCost, AsgQuery>> costEstimator;
    private static Object sync = new Object();
    //endregion
}
