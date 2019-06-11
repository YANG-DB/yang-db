package com.kayhut.fuse.epb.plan.extenders;

/*-
 * #%L
 * fuse-dv-epb
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

import com.kayhut.fuse.dispatcher.epb.PlanExtensionStrategy;
import com.kayhut.fuse.dispatcher.utils.PlanUtil;
import com.kayhut.fuse.model.asgQuery.AsgEBase;
import com.kayhut.fuse.model.asgQuery.AsgQuery;
import com.kayhut.fuse.model.execution.plan.composite.CompositePlanOp;
import com.kayhut.fuse.model.execution.plan.composite.Plan;
import com.kayhut.fuse.model.execution.plan.entity.EntityOp;
import com.kayhut.fuse.model.execution.plan.entity.GoToEntityOp;
import com.kayhut.fuse.model.query.entity.EEntityBase;
import javaslang.Tuple2;
import javaslang.collection.Map;
import javaslang.collection.Stream;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class GotoJoinExtensionStrategy implements PlanExtensionStrategy<Plan, AsgQuery>  {
    @Override
    public Iterable<Plan> extendPlan(Optional<Plan> plan, AsgQuery query) {
        if (!plan.isPresent()) {
            return Collections.emptyList();
        }

        List<Plan> plans = new ArrayList<>();

        EntityOp lastEntityOp = PlanUtil.last$(plan.get(), EntityOp.class);
        CompositePlanOp flattenedPlan = PlanUtil.flat(plan.get());


        Map<Integer, AsgEBase<EEntityBase>> entities = Stream.ofAll(flattenedPlan.getOps())
                .filter(op -> ((op instanceof EntityOp) && !(op instanceof GoToEntityOp) && !((EntityOp) op).getAsgEbase().equals(lastEntityOp.getAsgEbase())))
                .map(op -> (EntityOp) op)
                .toMap(op -> new Tuple2<>(op.getAsgEbase().geteNum(), op.getAsgEbase()));

        for (AsgEBase<EEntityBase> ancestor: entities.values()) {
            Plan newPlan = plan.get().withOp(new GoToEntityOp(ancestor));
            plans.add(newPlan);
        }

        return plans;
    }
}
