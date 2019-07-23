package com.yangdb.fuse.gta.strategy.discrete;

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

import com.yangdb.fuse.dispatcher.gta.TranslationContext;
import com.yangdb.fuse.gta.strategy.common.*;
import com.yangdb.fuse.gta.translation.ChainedPlanOpTraversalTranslator;
import com.yangdb.fuse.model.execution.plan.PlanOp;
import com.yangdb.fuse.model.execution.plan.PlanWithCost;
import com.yangdb.fuse.model.execution.plan.composite.Plan;
import com.yangdb.fuse.model.execution.plan.costs.PlanDetailedCost;
import com.yangdb.fuse.model.execution.plan.entity.EntityJoinOp;
import javaslang.collection.Stream;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;

/**
 * Created by Roman on 11/05/2017.
 */
public class M2PlanOpTranslationStrategy extends CompositePlanOpTranslationStrategy {
    //region Constructors
    public M2PlanOpTranslationStrategy() {
        super();

        this.strategies = Stream.of(
                new EntityOpTranslationStrategy(EntityTranslationOptions.none),
                new CompositePlanOpTranslationStrategy(
                        new EntityFilterOpTranslationStrategy(EntityTranslationOptions.none),
                        new EntitySelectionTranslationStrategy()
//                        new WhereByOpTranslationStrategy()
                ),
                new GoToEntityOpTranslationStrategy(),
                new RelationOpTranslationStrategy(),
                new CompositePlanOpTranslationStrategy(
                        new RelationFilterOpTranslationStrategy(),
                        new RelationSelectionTranslationStrategy()
//                        new WhereByOpTranslationStrategy()
                ),
                new OptionalOpTranslationStrategy(this),
                new CountOpTranslationStrategy(this),
                new UnionOpTranslationStrategy(this),
                new JoinEntityOpTranslationStrategy(new ChainedPlanOpTraversalTranslator(this), EntityJoinOp.class)
        ).toJavaList();
    }
    //endregion


    @Override
    public GraphTraversal translate(GraphTraversal traversal, PlanWithCost<Plan, PlanDetailedCost> plan, PlanOp planOp, TranslationContext context) {
        return super.translate(traversal, plan, planOp, context);
    }
}
