package com.yangdb.fuse.epb.plan.extenders;

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

import com.yangdb.fuse.model.asgQuery.AsgQueryUtil;
import com.yangdb.fuse.dispatcher.epb.PlanExtensionStrategy;
import com.yangdb.fuse.model.asgQuery.AsgEBase;
import com.yangdb.fuse.model.asgQuery.AsgQuery;
import com.yangdb.fuse.model.execution.plan.composite.Plan;
import com.yangdb.fuse.model.execution.plan.entity.EntityFilterOp;
import com.yangdb.fuse.model.execution.plan.entity.EntityOp;
import com.yangdb.fuse.model.execution.plan.relation.RelationFilterOp;
import com.yangdb.fuse.model.execution.plan.relation.RelationOp;
import com.yangdb.fuse.model.query.Rel;
import com.yangdb.fuse.model.query.entity.EEntityBase;
import com.yangdb.fuse.model.query.properties.EPropGroup;
import com.yangdb.fuse.model.query.properties.RelPropGroup;
import com.yangdb.fuse.model.query.quant.Quant1;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static com.yangdb.fuse.epb.plan.extenders.SimpleExtenderUtils.getNextDescendantsUnmarkedOfType;

/**
 * Created by Roman on 23/04/2017.
 */
public class StepDescendantsAdjacentStrategy implements PlanExtensionStrategy<Plan, AsgQuery> {

    //region PlanExtensionStrategy Implementation
    @Override
    public Iterable<Plan> extendPlan(Optional<Plan> plan, AsgQuery query) {
        if (!plan.isPresent()) {
            return Collections.emptyList();
        }

        List<AsgEBase<Rel>> nextRelations = getNextDescendantsUnmarkedOfType(plan.get(), Rel.class);

        if (nextRelations.isEmpty()) {
            return Collections.emptyList();
        }
        List<Plan> plans = new ArrayList<>();

        Plan newPlan = plan.get();
        for (AsgEBase<Rel> nextRelation : nextRelations) {
            Optional<Plan> computedPlan = compute(nextRelation, newPlan);
            if (computedPlan.isPresent()) {
                plans.add(computedPlan.get());
            }
        }

        return plans;
    }

    private Optional<Plan> compute(AsgEBase<Rel> nextRelation, Plan newPlan) {

        Optional<AsgEBase<RelPropGroup>> nextRelationPropGroup = AsgQueryUtil.bDescendant(nextRelation, RelPropGroup.class);

        Optional<AsgEBase<EEntityBase>> toEntity = AsgQueryUtil.nextDescendant(nextRelation, EEntityBase.class);
        if (!toEntity.isPresent()) {
            return Optional.empty();
        }

        Optional<AsgEBase<Quant1>> toEntityQuant = AsgQueryUtil.nextAdjacentDescendant(toEntity.get(), Quant1.class);
        Optional<AsgEBase<EPropGroup>> toEntityPropGroup = toEntityQuant.isPresent() ?
                AsgQueryUtil.nextAdjacentDescendant(toEntityQuant.get(), EPropGroup.class) :
                AsgQueryUtil.nextAdjacentDescendant(toEntity.get(), EPropGroup.class);

        newPlan = newPlan.withOp(new RelationOp(nextRelation));
        if (nextRelationPropGroup.isPresent()) {
            newPlan = newPlan.withOp(new RelationFilterOp(nextRelationPropGroup.get()));
        }

        newPlan = newPlan.withOp(new EntityOp(toEntity.get()));
        if (toEntityPropGroup.isPresent()) {
            newPlan = newPlan.withOp(new EntityFilterOp(toEntityPropGroup.get()));
        }

        return Optional.of(newPlan);
    }
    //endregion

}
