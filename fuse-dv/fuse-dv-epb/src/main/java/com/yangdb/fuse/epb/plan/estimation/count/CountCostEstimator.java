package com.yangdb.fuse.epb.plan.estimation.count;

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

import com.google.inject.Inject;
import com.yangdb.fuse.dispatcher.epb.CostEstimator;
import com.yangdb.fuse.dispatcher.gta.PlanTraversalTranslator;
import com.yangdb.fuse.dispatcher.gta.TranslationContext;
import com.yangdb.fuse.dispatcher.ontology.OntologyProvider;
import com.yangdb.fuse.epb.plan.estimation.IncrementalEstimationContext;
import com.yangdb.fuse.executor.ontology.UniGraphProvider;
import com.yangdb.fuse.model.asgQuery.AsgQuery;
import com.yangdb.fuse.model.execution.plan.PlanWithCost;
import com.yangdb.fuse.model.execution.plan.composite.Plan;
import com.yangdb.fuse.model.execution.plan.costs.DoubleCost;
import com.yangdb.fuse.model.execution.plan.costs.PlanDetailedCost;
import com.yangdb.fuse.model.ontology.Ontology;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.unipop.process.Profiler;

import java.util.Collections;

import static org.unipop.process.Profiler.PROFILER;

/**
 * Created by Roman on 3/14/2018.
 */
public class CountCostEstimator implements CostEstimator<Plan, PlanDetailedCost, IncrementalEstimationContext<Plan, PlanDetailedCost, AsgQuery>> {
    //region Constructors
    @Inject
    public CountCostEstimator(
            OntologyProvider ontologyProvider,
            PlanTraversalTranslator planTraversalTranslator,
            UniGraphProvider uniGraphProvider) {

        this.ontologyProvider = ontologyProvider;
        this.planTraversalTranslator = planTraversalTranslator;
        this.uniGraphProvider = uniGraphProvider;
    }
    //endregion

    //region CostEstimator Implementation
    @Override
    public PlanWithCost<Plan, PlanDetailedCost> estimate(Plan plan, IncrementalEstimationContext<Plan, PlanDetailedCost, AsgQuery> estimationContext) {
        Ontology ontology = this.ontologyProvider.get(estimationContext.getQuery().getOnt()).get();
        GraphTraversal<?, ?> traversal = null;
        try {
            traversal = this.planTraversalTranslator.translate(
                    new PlanWithCost<>(plan, new PlanDetailedCost(new DoubleCost(0.0), Collections.emptyList())),
                    new TranslationContext(
                            new Ontology.Accessor(ontology),
                            uniGraphProvider.getGraph(ontology).traversal()));
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
        //todo add configuration activation
        traversal.asAdmin().getSideEffects().register(PROFILER, Profiler.Impl::new, null);

        long count = traversal.count().next();

        //Todo log profiler
        Profiler profiler = traversal.asAdmin().getSideEffects().getOrCreate(PROFILER, Profiler.Impl::new);
        System.out.println(profiler);

        return new PlanWithCost<>(plan, new PlanDetailedCost(new DoubleCost(count), Collections.emptyList()));
    }
    //endregion

    //region Fields
    private OntologyProvider ontologyProvider;
    private PlanTraversalTranslator planTraversalTranslator;
    private UniGraphProvider uniGraphProvider;
    //endregion
}
