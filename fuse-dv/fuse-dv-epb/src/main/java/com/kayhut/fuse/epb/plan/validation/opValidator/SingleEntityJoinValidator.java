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

import com.kayhut.fuse.epb.plan.validation.ChainedPlanValidator;
import com.kayhut.fuse.model.asgQuery.AsgQuery;
import com.kayhut.fuse.model.execution.plan.PlanOp;
import com.kayhut.fuse.model.execution.plan.composite.CompositePlanOp;
import com.kayhut.fuse.model.execution.plan.composite.descriptors.IterablePlanOpDescriptor;
import com.kayhut.fuse.model.execution.plan.entity.EntityJoinOp;
import com.kayhut.fuse.model.execution.plan.entity.EntityOp;
import com.kayhut.fuse.model.validation.ValidationResult;

import java.util.List;

import static java.util.stream.Collectors.toList;

/**
 * Validates a complete join operation that neither of its branches has a single entity (useless join)
 */
public class SingleEntityJoinValidator implements ChainedPlanValidator.PlanOpValidator {
    @Override
    public void reset() {

    }

    @Override
    public ValidationResult isPlanOpValid(AsgQuery query, CompositePlanOp compositePlanOp, int opIndex) {
        if(compositePlanOp.getOps().get(opIndex) instanceof EntityJoinOp && !checkJoin((EntityJoinOp) compositePlanOp.getOps().get(opIndex))){
            return new ValidationResult(false,this.getClass().getSimpleName(), "A complete join cannot have a single entity in one of its branches, " + IterablePlanOpDescriptor.getSimple().describe(compositePlanOp.getOps()));
        }
        return ValidationResult.OK;
    }

    private boolean checkJoin(EntityJoinOp joinOp){
        boolean valid = true;
        List<PlanOp> leftEntities = joinOp.getLeftBranch().getOps().stream().filter(op -> op instanceof EntityOp).collect(toList());
        if(leftEntities.size() == 1 && leftEntities.get(0).getClass().equals(EntityOp.class))
            return false;
        if(joinOp.isComplete()){
            List<PlanOp> rightEntities = joinOp.getRightBranch().getOps().stream().filter(op -> op instanceof EntityOp).collect(toList());
            if(rightEntities.size() == 1 && rightEntities.get(0).getClass().equals(EntityOp.class))
                return false;
        }

        return valid;
    }
}
