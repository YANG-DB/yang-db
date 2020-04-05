package com.yangdb.fuse.gta.strategy.discrete;

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

import com.yangdb.fuse.dispatcher.utils.PlanUtil;
import com.yangdb.fuse.gta.strategy.PlanOpTranslationStrategyBase;
import com.yangdb.fuse.gta.strategy.common.EntityTranslationOptions;
import com.yangdb.fuse.gta.strategy.utils.EntityTranslationUtil;
import com.yangdb.fuse.dispatcher.gta.TranslationContext;
import com.yangdb.fuse.model.execution.plan.*;
import com.yangdb.fuse.model.execution.plan.composite.Plan;
import com.yangdb.fuse.model.execution.plan.costs.PlanDetailedCost;
import com.yangdb.fuse.model.execution.plan.entity.EntityOp;
import com.yangdb.fuse.model.execution.plan.relation.RelationFilterOp;
import com.yangdb.fuse.model.execution.plan.relation.RelationOp;
import com.yangdb.fuse.model.ontology.Ontology;
import com.yangdb.fuse.model.query.entity.*;
import com.yangdb.fuse.unipop.controller.promise.GlobalConstants;
import com.yangdb.fuse.unipop.promise.Constraint;
import org.apache.tinkerpop.gremlin.process.traversal.P;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.structure.T;
import com.yangdb.fuse.unipop.process.traversal.dsl.graph.__;

import java.util.List;
import java.util.Optional;

/**
 * Created by Roman on 10/05/2017.
 */
public class EntityOpTranslationStrategy extends PlanOpTranslationStrategyBase {
    //region Constructors
    public EntityOpTranslationStrategy(EntityTranslationOptions options) {
        super(planOp -> planOp.getClass().equals(EntityOp.class));
        this.options = options;
    }
    //endregion

    //region PlanOpTranslationStrategy Implementation
    @Override
    protected GraphTraversal translateImpl(GraphTraversal traversal, PlanWithCost<Plan, PlanDetailedCost> plan, PlanOp planOp, TranslationContext context) {
        EntityOp entityOp = (EntityOp)planOp;

        if (PlanUtil.isFirst(plan.getPlan(), planOp)) {
            traversal = context.getGraphTraversalSource().V().as(entityOp.getAsgEbase().geteBase().geteTag());
            appendEntity(traversal, entityOp.getAsgEbase().geteBase(), context.getOnt());
        } else {
            Optional<PlanOp> previousPlanOp = PlanUtil.adjacentPrev(plan.getPlan(), planOp);
            if (previousPlanOp.isPresent() &&
                    (previousPlanOp.get() instanceof RelationOp ||
                            previousPlanOp.get() instanceof RelationFilterOp)) {
                switch (this.options) {
                    case none: return traversal.otherV().as(entityOp.getAsgEbase().geteBase().geteTag());
                    case filterEntity:
                        traversal.otherV();
                        traversal.outE(GlobalConstants.Labels.PROMISE_FILTER);
                        appendEntity(traversal, entityOp.getAsgEbase().geteBase(), context.getOnt());
                        traversal.otherV().as(entityOp.getAsgEbase().geteBase().geteTag());
                }
            }
        }

        return traversal;
    }
    //endregion

    //region Private Methods
    private GraphTraversal appendEntity(GraphTraversal traversal,
                                        EEntityBase entity,
                                        Ontology.Accessor ont) {

        if (entity instanceof EConcrete) {
            traversal.has(GlobalConstants.HasKeys.CONSTRAINT,
                    P.eq(Constraint.by(__.start().and(
                            __.start().has(T.id, P.eq(((EConcrete)entity).geteID())),
                            __.start().has(T.label, P.eq(EntityTranslationUtil.getValidEntityNames(ont, entity).get(0)))))));
        }
        else if (entity instanceof ETyped || entity instanceof EUntyped) {
            List<String> eTypeNames = EntityTranslationUtil.getValidEntityNames(ont, entity);
            if (eTypeNames.isEmpty()) {
                traversal.has(GlobalConstants.HasKeys.CONSTRAINT,
                        Constraint.by(__.start().has(T.label, P.eq(GlobalConstants.Labels.NONE))));
            } else if (eTypeNames.size() == 1) {
                traversal.has(GlobalConstants.HasKeys.CONSTRAINT,
                        Constraint.by(__.start().has(T.label, P.eq(eTypeNames.get(0)))));
            } else if (eTypeNames.size() > 1) {
                traversal.has(GlobalConstants.HasKeys.CONSTRAINT,
                        Constraint.by(__.start().has(T.label, P.within(eTypeNames))));
            }
        }

        return traversal;
    }
    //endregion

    //region Fields
    private EntityTranslationOptions options;
    //endregion
}
