package com.yangdb.fuse.epb.plan.pruners;

/*-
 *
 * fuse-dv-epb
 * %%
 * Copyright (C) 2016 - 2019 yangdb   ------ www.yangdb.org ------
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
 *
 */

import com.yangdb.fuse.dispatcher.epb.PlanPruneStrategy;
import com.yangdb.fuse.dispatcher.utils.PlanUtil;
import com.yangdb.fuse.model.execution.plan.PlanWithCost;
import com.yangdb.fuse.model.execution.plan.composite.Plan;
import com.yangdb.fuse.model.execution.plan.composite.descriptors.IterablePlanOpDescriptor;
import com.yangdb.fuse.model.execution.plan.costs.PlanDetailedCost;
import com.yangdb.fuse.model.execution.plan.entity.EntityJoinOp;
import javaslang.collection.Stream;

import java.util.*;

/**
 * Removes symmetrical join plans. This pruner assumes that all plans which have a join as their
 */
public class SymmetricalJoinPruner implements PlanPruneStrategy<PlanWithCost<Plan, PlanDetailedCost>> {
    @Override
    public Iterable<PlanWithCost<Plan, PlanDetailedCost>> prunePlans(Iterable<PlanWithCost<Plan, PlanDetailedCost>> plans) {
     return Stream.ofAll(plans).filter(plan -> {
            if(plan.getPlan().getOps().size() == 1) {
                Optional<EntityJoinOp> joinOp = PlanUtil.first(plan.getPlan(), EntityJoinOp.class);
                if(joinOp.isPresent() && joinOp.get().isComplete()){
                    String leftDescription = IterablePlanOpDescriptor.getFull().describe(Collections.singleton(joinOp.get().getLeftBranch()));
                    String rightDescription = IterablePlanOpDescriptor.getFull().describe(Collections.singleton(joinOp.get().getRightBranch()));
                    return leftDescription.compareTo(rightDescription) < 0;
                }
            }
            return true;
        });
    }
}
