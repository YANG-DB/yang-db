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

import com.yangdb.fuse.dispatcher.epb.PlanExtensionStrategy;
import com.yangdb.fuse.dispatcher.utils.PlanUtil;
import com.yangdb.fuse.model.asgQuery.AsgQuery;
import com.yangdb.fuse.model.execution.plan.composite.Plan;
import com.yangdb.fuse.model.execution.plan.entity.EntityJoinOp;
import javaslang.collection.Stream;

import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

/**
 * Created by moti on 7/3/2017.
 * Extends join ops that are the last op in the plan
 */
public class JoinOngoingExtensionStrategy implements PlanExtensionStrategy<Plan, AsgQuery> {
    private PlanExtensionStrategy<Plan, AsgQuery> innerExpander;

    public JoinOngoingExtensionStrategy(PlanExtensionStrategy<Plan, AsgQuery> innerExpander) {
        this.innerExpander = innerExpander;
    }

    @Override
    public Iterable<Plan> extendPlan(Optional<Plan> plan, AsgQuery query) {
        // Cannot continue a join from an empty plan
        if(!plan.isPresent() || plan.get().getOps().isEmpty()) {
            return Collections.emptyList();
        }

        // Check if the plan has a Join op and this is the last op
        if (plan.get().getOps().size() == 1 && PlanUtil.last(plan.get(), EntityJoinOp.class).isPresent()) {
            EntityJoinOp joinOp = PlanUtil.last(plan.get(), EntityJoinOp.class).get();
            //TODO: maybe this if should not be here
            if(joinOp.isComplete()) {
                // Can't continue a join which is complete
                return Collections.emptyList();
            }

            // extend right branch and create new plans
            return Stream.ofAll(innerExpander.extendPlan(Optional.of(joinOp.getRightBranch()), query))
                    .map(extendedRightBranch -> new Plan(new EntityJoinOp(joinOp.getLeftBranch(), extendedRightBranch)))
                    .flatMap(extendPlan -> {
                        if(EntityJoinOp.isComplete((EntityJoinOp) extendPlan.getOps().get(0))){
                            return Arrays.asList(extendPlan,
                                    new Plan(new EntityJoinOp(((EntityJoinOp) extendPlan.getOps().get(0)).getLeftBranch(), ((EntityJoinOp) extendPlan.getOps().get(0)).getRightBranch(),true)));
                        }else{
                            return Collections.singletonList(extendPlan);
                        }
                    })
                    .toJavaList();
        }

        return Collections.emptyList();
    }
}
