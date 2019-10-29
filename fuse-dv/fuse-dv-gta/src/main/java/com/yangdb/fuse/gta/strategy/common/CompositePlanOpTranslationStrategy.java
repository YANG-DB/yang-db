package com.yangdb.fuse.gta.strategy.common;

/*-
 * #%L
 * fuse-dv-gta
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

import com.yangdb.fuse.gta.strategy.PlanOpTranslationStrategy;
import com.yangdb.fuse.dispatcher.gta.TranslationContext;
import com.yangdb.fuse.model.execution.plan.composite.Plan;
import com.yangdb.fuse.model.execution.plan.PlanOp;
import com.yangdb.fuse.model.execution.plan.PlanWithCost;
import com.yangdb.fuse.model.execution.plan.costs.PlanDetailedCost;
import javaslang.collection.Stream;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;

/**
 * Created by Roman on 10/05/2017.
 */
public class CompositePlanOpTranslationStrategy implements PlanOpTranslationStrategy {
    //region Constructors
    public CompositePlanOpTranslationStrategy(PlanOpTranslationStrategy...strategies) {
        this.strategies = Stream.of(strategies).toJavaList();
    }

    public CompositePlanOpTranslationStrategy(Iterable<PlanOpTranslationStrategy> strategies) {
        this.strategies = Stream.ofAll(strategies).toJavaList();
    }
    //endregion

    //region PlanOpTranslationStrategy Implementation
    @Override
    public GraphTraversal translate(GraphTraversal traversal, PlanWithCost<Plan,PlanDetailedCost> plan, PlanOp planOp, TranslationContext context) {
        for(PlanOpTranslationStrategy planOpTranslationStrategy : this.strategies) {
            traversal = planOpTranslationStrategy.translate(traversal, plan, planOp, context);
        }

        return traversal;
    }
    //endregion

    //region Fields
    protected Iterable<PlanOpTranslationStrategy> strategies;
    //endregion
}
