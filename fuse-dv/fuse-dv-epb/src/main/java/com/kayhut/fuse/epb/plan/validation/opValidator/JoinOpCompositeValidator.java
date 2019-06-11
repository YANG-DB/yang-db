package com.kayhut.fuse.epb.plan.validation.opValidator;

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

import com.kayhut.fuse.dispatcher.epb.PlanValidator;
import com.kayhut.fuse.epb.plan.validation.ChainedPlanValidator;
import com.kayhut.fuse.model.asgQuery.AsgQuery;
import com.kayhut.fuse.model.execution.plan.PlanOp;
import com.kayhut.fuse.model.execution.plan.composite.CompositePlanOp;
import com.kayhut.fuse.model.execution.plan.composite.Plan;
import com.kayhut.fuse.model.execution.plan.entity.EntityJoinOp;
import com.kayhut.fuse.model.validation.ValidationResult;
import javaslang.collection.Stream;

/**
 * Validates join op branches with internal validators
 */
public class JoinOpCompositeValidator implements ChainedPlanValidator.PlanOpValidator{
    private PlanValidator<Plan, AsgQuery> leftValidator;
    private PlanValidator<Plan, AsgQuery> rightValidator;

    public JoinOpCompositeValidator(PlanValidator<Plan, AsgQuery> leftValidator, PlanValidator<Plan, AsgQuery> rightValidator) {
        this.leftValidator = leftValidator;
        this.rightValidator = rightValidator;
    }

    @Override
    public void reset() {

    }

    @Override
    public ValidationResult isPlanOpValid(AsgQuery query, CompositePlanOp compositePlanOp, int opIndex) {
        PlanOp planOp = compositePlanOp.getOps().get(opIndex);
        if(planOp instanceof EntityJoinOp){
            EntityJoinOp joinOp = (EntityJoinOp) planOp;
            ValidationResult leftValidationContext = this.leftValidator.isPlanValid(joinOp.getLeftBranch(), query);
            ValidationResult rightValidationContext = this.rightValidator.isPlanValid(joinOp.getRightBranch(), query);
            return new ValidationResult(leftValidationContext.valid() && rightValidationContext.valid(),
                    this.getClass().getSimpleName(),
                    Stream.ofAll(leftValidationContext.errors()).appendAll(rightValidationContext.errors()));

        }
        return ValidationResult.OK;
    }
}
