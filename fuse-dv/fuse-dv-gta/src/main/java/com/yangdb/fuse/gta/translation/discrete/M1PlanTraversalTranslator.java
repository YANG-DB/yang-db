package com.yangdb.fuse.gta.translation.discrete;

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

import com.yangdb.fuse.gta.strategy.discrete.M1PlanOpTranslationStrategy;
import com.yangdb.fuse.gta.translation.ChainedPlanOpTraversalTranslator;
import com.yangdb.fuse.dispatcher.gta.TranslationContext;
import com.yangdb.fuse.model.execution.plan.composite.Plan;
import com.yangdb.fuse.model.execution.plan.PlanWithCost;
import com.yangdb.fuse.model.execution.plan.costs.PlanDetailedCost;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;

/**
 * Created by Roman on 28/06/2017.
 */
public class M1PlanTraversalTranslator extends ChainedPlanOpTraversalTranslator {
    //region Constructors
    public M1PlanTraversalTranslator() {
        super(new M1PlanOpTranslationStrategy());
    }
    //endregion

    //region Override Methods
    @Override
    public GraphTraversal<?, ?> translate(PlanWithCost<Plan, PlanDetailedCost> planWithCost, TranslationContext context) {
        return super.translate(planWithCost, context);
    }
    //endregion
}
