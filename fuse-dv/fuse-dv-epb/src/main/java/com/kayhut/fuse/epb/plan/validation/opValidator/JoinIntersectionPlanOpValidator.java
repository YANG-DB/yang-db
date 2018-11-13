package com.kayhut.fuse.epb.plan.validation.opValidator;

/*-
 * #%L
 * fuse-dv-epb
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
import com.kayhut.fuse.epb.plan.validation.ChainedPlanValidator;
import com.kayhut.fuse.model.asgQuery.AsgQuery;
import com.kayhut.fuse.model.execution.plan.PlanOp;
import com.kayhut.fuse.model.execution.plan.composite.CompositePlanOp;
import com.kayhut.fuse.model.execution.plan.composite.Plan;
import com.kayhut.fuse.model.execution.plan.composite.descriptors.IterablePlanOpDescriptor;
import com.kayhut.fuse.model.execution.plan.entity.EntityJoinOp;
import com.kayhut.fuse.model.execution.plan.entity.EntityOp;
import com.kayhut.fuse.model.log.Trace;
import com.kayhut.fuse.model.validation.ValidationResult;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Created by benishue on 7/5/2017.
 */
public class JoinIntersectionPlanOpValidator implements ChainedPlanValidator.PlanOpValidator {
    private Trace<String> trace = Trace.build(JoinIntersectionPlanOpValidator.class.getSimpleName());


    //region Private Methods
    private boolean isIntersectionValid(EntityJoinOp joinOp) {
        Optional<EntityOp> leftLast = PlanUtil.last(joinOp.getLeftBranch(), EntityOp.class);
        Optional<EntityOp> rightLast = PlanUtil.last(joinOp.getRightBranch(), EntityOp.class);
        Set<Integer> leftEopSet = getEntityOpsRecursively(joinOp.getLeftBranch().getOps(), new HashSet<>());
        Set<Integer> rightEopSet = getEntityOpsRecursively(joinOp.getRightBranch().getOps(), new HashSet<>());

        Set<Integer> intersection = new HashSet<>(leftEopSet);
        intersection.retainAll(rightEopSet);

        //0 intersection is OK, since we can be in a state where we didn't finish yet.
        if (intersection.size() == 0) {
            return true;
        }
        if (intersection.size() == 1) {
            return leftLast.isPresent() &&
                    rightLast.isPresent() &&
                    leftLast.get().getAsgEbase().geteNum() == rightLast.get().getAsgEbase().geteNum() &&
                    !joinOp.getLeftBranch().equals(joinOp.getRightBranch());
        }

        return false;
    }

    private Set<Integer> getEntityOpsRecursively(List<PlanOp> ops, Set<Integer> set) {
        for (PlanOp op : ops) {
            if (op instanceof EntityOp) {
                set.add(((EntityOp)op).getAsgEbase().geteNum());
            }
            if (op instanceof EntityJoinOp) {
                getEntityOpsRecursively(((EntityJoinOp) op).getLeftBranch().getOps(), set);
                getEntityOpsRecursively(((EntityJoinOp) op).getRightBranch().getOps(), set);
            }
        }
        return set;
    }

    @Override
    public void reset() {

    }

    @Override
    public ValidationResult isPlanOpValid(AsgQuery query, CompositePlanOp compositePlanOp, int opIndex) {
        PlanOp planOp = compositePlanOp.getOps().get(opIndex);
        if(planOp instanceof EntityJoinOp) {
            EntityJoinOp joinOp = (EntityJoinOp) planOp;
            if (compositePlanOp.getOps().size() == 1 && !isIntersectionValid(joinOp)) {
                return new ValidationResult(false,this.getClass().getSimpleName(), "JoinOp intersection validation failed: " + IterablePlanOpDescriptor.getSimple().describe(compositePlanOp.getOps()));
            }
        }
        return ValidationResult.OK;
    }

    //endregion

}
