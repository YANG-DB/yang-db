package com.yangdb.fuse.gta.strategy.common;

/*-
 * #%L
 * fuse-dv-gta
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

import com.yangdb.fuse.dispatcher.gta.PlanTraversalTranslator;
import com.yangdb.fuse.dispatcher.gta.TranslationContext;
import com.yangdb.fuse.gta.strategy.PlanOpTranslationStrategy;
import com.yangdb.fuse.gta.strategy.PlanOpTranslationStrategyBase;
import com.yangdb.fuse.gta.translation.ChainedPlanOpTraversalTranslator;
import com.yangdb.fuse.model.execution.plan.PlanOp;
import com.yangdb.fuse.model.execution.plan.PlanWithCost;
import com.yangdb.fuse.model.execution.plan.composite.Plan;
import com.yangdb.fuse.model.execution.plan.composite.UnionOp;
import com.yangdb.fuse.model.execution.plan.costs.PlanDetailedCost;
import com.yangdb.fuse.unipop.process.traversal.dsl.graph.FuseGraphTraversalSource;
import javaslang.collection.Stream;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;

import java.util.List;

public class UnionOpTranslationStrategy extends PlanOpTranslationStrategyBase {
    private final PlanOpTranslationStrategy planOpTranslationStrategy;

    public UnionOpTranslationStrategy(PlanOpTranslationStrategy planOpTranslationStrategy) {
        super(planOp -> planOp.getClass().equals(UnionOp.class));
        this.planOpTranslationStrategy = planOpTranslationStrategy;
    }

    @Override
    protected GraphTraversal translateImpl(GraphTraversal traversal, PlanWithCost<Plan, PlanDetailedCost> planWithCost, PlanOp planOp, TranslationContext context) {
        UnionOp unionOp = (UnionOp)planOp;

        int indexOfUnion = planWithCost.getPlan().getOps().indexOf(planOp);
        final List<? extends GraphTraversal<?, ?>> traversalList = Stream.ofAll(unionOp.getPlans()).map(plan -> {
            //create a (one of) branch union plan
            Plan unionPlan = new Plan(Stream.ofAll(planWithCost.getPlan().getOps()).take(indexOfUnion).toJavaList()).append(plan);
            //create chained plan translator
            PlanTraversalTranslator planTraversalTranslator = new ChainedPlanOpTraversalTranslator(this.planOpTranslationStrategy, indexOfUnion);
            //return the gremlin translated plan
            return planTraversalTranslator.translate(new PlanWithCost<>(unionPlan, planWithCost.getCost()), context);
        }).toJavaList();
        //traversal union translated branches
        return ((FuseGraphTraversalSource) context.getGraphTraversalSource()).union(traversalList.toArray(new GraphTraversal[traversalList.size()]));
    }
}
