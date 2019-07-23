package com.yangdb.fuse.epb.plan.extenders;

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

import com.yangdb.fuse.dispatcher.utils.PlanUtil;
import com.yangdb.fuse.dispatcher.epb.PlanExtensionStrategy;
import com.yangdb.fuse.model.asgQuery.AsgQuery;
import com.yangdb.fuse.model.execution.plan.entity.EntityOp;
import com.yangdb.fuse.model.execution.plan.entity.GoToEntityOp;
import com.yangdb.fuse.model.execution.plan.composite.Plan;
import javaslang.collection.Stream;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class GotoExtensionStrategy implements PlanExtensionStrategy<Plan, AsgQuery> {
    private boolean addInitialPlan;

    public GotoExtensionStrategy(boolean addInitialPlan) {
        this.addInitialPlan = addInitialPlan;
    }

    public GotoExtensionStrategy() {
        this(false);
    }

    @Override
    public Iterable<Plan> extendPlan(Optional<Plan> plan, AsgQuery query) {
        if (!plan.isPresent()) {
            return Collections.emptyList();
        }

        List<Plan> plans = new ArrayList<>();

        EntityOp lastEntityOp = PlanUtil.last$(plan.get(), EntityOp.class);

        List<EntityOp> entityOps = Stream.ofAll(plan.get().getOps())
                .filter(op -> ((op instanceof EntityOp) && !(op instanceof GoToEntityOp) && !op.equals(lastEntityOp)))
                .map(op -> (EntityOp)op)
                .toJavaList();

        for (EntityOp ancestorEntityOp : entityOps) {
            Plan newPlan = plan.get().withOp(new GoToEntityOp(ancestorEntityOp.getAsgEbase()));

            plans.add(newPlan);
        }

        if(this.addInitialPlan){
            plans.add(plan.get());
        }

        return plans;
    }
}
