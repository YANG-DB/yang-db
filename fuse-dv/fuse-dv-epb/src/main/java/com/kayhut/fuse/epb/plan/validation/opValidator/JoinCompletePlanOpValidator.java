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

import com.kayhut.fuse.dispatcher.epb.PlanValidator;
import com.kayhut.fuse.dispatcher.utils.PlanUtil;
import com.kayhut.fuse.epb.plan.validation.ChainedPlanValidator;
import com.kayhut.fuse.model.asgQuery.AsgQuery;
import com.kayhut.fuse.model.execution.plan.PlanOp;
import com.kayhut.fuse.model.execution.plan.composite.CompositePlanOp;
import com.kayhut.fuse.model.execution.plan.composite.Plan;
import com.kayhut.fuse.model.execution.plan.composite.descriptors.IterablePlanOpDescriptor;
import com.kayhut.fuse.model.execution.plan.entity.EntityFilterOp;
import com.kayhut.fuse.model.execution.plan.entity.EntityJoinOp;
import com.kayhut.fuse.model.execution.plan.entity.EntityOp;
import com.kayhut.fuse.model.log.Trace;
import com.kayhut.fuse.model.validation.ValidationResult;

import java.util.Optional;



/**
 * Created by benishue on 7/4/2017.
 */
public class JoinCompletePlanOpValidator implements ChainedPlanValidator.PlanOpValidator {
    //region Private Methods
    private boolean validateIfOnlyJoin = false;

    public JoinCompletePlanOpValidator() {
        validateIfOnlyJoin = false;
    }

    public JoinCompletePlanOpValidator(boolean validateIfOnlyJoin) {
        this.validateIfOnlyJoin = validateIfOnlyJoin;
    }

    /*
         * "Complete Join Op" - on the left branch of the JoinOp we are looking
         * for the last EntityOp(EOP) or EOP + attached EntityFilterOp (EFO).
         * We should check that we have this EOP (or EOP + EFO) at the right branch of the JoinOp.
         * i.e., The enums should be the same.
         */
    //TODO: use EntityJoinOp.isComplete instead
    private boolean isJoinOpComplete(EntityJoinOp joinOp) {
        return joinOp.isComplete();
    }

    @Override
    public void reset() {

    }

    @Override
    public ValidationResult isPlanOpValid(AsgQuery query, CompositePlanOp compositePlanOp, int opIndex) {
        if(opIndex > 0)
            return ValidationResult.OK;
        PlanOp planOp = compositePlanOp.getOps().get(opIndex);
        if(planOp instanceof EntityJoinOp) {
            EntityJoinOp joinOp = (EntityJoinOp) planOp;
            if ((compositePlanOp.getOps().size() > 1 || validateIfOnlyJoin) && !isJoinOpComplete(joinOp)) {
                return new ValidationResult(false,this.getClass().getSimpleName(), "JoinOp complete validation failed: " + IterablePlanOpDescriptor.getSimple().describe(compositePlanOp.getOps()));
            }
        }
        return ValidationResult.OK;
    }

    //endregion

}
