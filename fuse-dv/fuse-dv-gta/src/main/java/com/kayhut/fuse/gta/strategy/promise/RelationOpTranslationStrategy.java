package com.kayhut.fuse.gta.strategy.promise;

/*-
 * #%L
 * fuse-dv-gta
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

import com.kayhut.fuse.dispatcher.utils.PlanUtil;
import com.kayhut.fuse.gta.strategy.PlanOpTranslationStrategyBase;
import com.kayhut.fuse.gta.strategy.utils.ConversionUtil;
import com.kayhut.fuse.dispatcher.gta.TranslationContext;
import com.kayhut.fuse.model.execution.plan.*;
import com.kayhut.fuse.model.execution.plan.composite.Plan;
import com.kayhut.fuse.model.execution.plan.costs.PlanDetailedCost;
import com.kayhut.fuse.model.execution.plan.entity.EntityOp;
import com.kayhut.fuse.model.execution.plan.relation.RelationOp;
import com.kayhut.fuse.model.query.Rel;
import com.kayhut.fuse.model.query.entity.EEntityBase;
import com.kayhut.fuse.unipop.controller.promise.GlobalConstants;
import com.kayhut.fuse.unipop.promise.Constraint;
import org.apache.tinkerpop.gremlin.process.traversal.P;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__;
import org.apache.tinkerpop.gremlin.structure.T;

import java.util.Optional;

/**
 * Created by benishue on 08-Mar-17.
 *
 *
 *
 * relOp = outE('promise').has('constraint', P.eq(Constraint.by(__.and(
 *                  __.has('label', P.eq(<Ontology(<rel.rType>)>)),
 *                  __.has('direction', P.eq(TinkerPop.direction(<Rel.dir>)))))))
 *                  .as(source(<EEntityBase.Etag>)-->target(<EEntityBase.Etag></EEntityBase.Etag>))  // A-->B
 *
 */
public class RelationOpTranslationStrategy extends PlanOpTranslationStrategyBase {
    //region Constructors
    public RelationOpTranslationStrategy() {
        super(RelationOp.class);
    }
    //endregion

    //region PlanOpTranslationStrategy Implementation
    @Override
    protected GraphTraversal translateImpl(GraphTraversal traversal, PlanWithCost<Plan, PlanDetailedCost> plan, PlanOp planOp, TranslationContext context) {
        Optional<EntityOp> prev = PlanUtil.prev(plan.getPlan(), planOp, EntityOp.class);
        Optional<EntityOp> next = PlanUtil.next(plan.getPlan(), planOp, EntityOp.class);

        Rel rel = ((RelationOp)planOp).getAsgEbase().geteBase();
        String rTypeName = context.getOnt().$relation$(rel.getrType()).getName();
        return traversal.outE(GlobalConstants.Labels.PROMISE)
                .as(createLabelForRelation(prev.get().getAsgEbase().geteBase(), next.get().getAsgEbase().geteBase()))
                .has(GlobalConstants.HasKeys.CONSTRAINT,
                        Constraint.by(__.and(
                                __.has(T.label, P.eq(rTypeName)),
                                __.has(GlobalConstants.HasKeys.DIRECTION, ConversionUtil.convertDirection(rel.getDir())))));

    }
    //endregion

    //region Private Methods
    private String createLabelForRelation(EEntityBase prev, EEntityBase next) {
        return prev.geteTag() + "-->" + next.geteTag();
    }
    //endregion
}
