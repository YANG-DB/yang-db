package com.yangdb.fuse.epb.plan.validation.opValidator;

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

import com.yangdb.fuse.epb.plan.validation.ChainedPlanValidator;
import com.yangdb.fuse.model.asgQuery.AsgQuery;
import com.yangdb.fuse.model.execution.plan.composite.CompositePlanOp;
import com.yangdb.fuse.model.execution.plan.entity.EntityOp;
import com.yangdb.fuse.model.query.entity.EEntityBase;
import com.yangdb.fuse.model.validation.ValidationResult;

/**
 * Validates a single entity op is always accompanied with an EProp
 */
public class SingleEntityValidator implements ChainedPlanValidator.PlanOpValidator {
    @Override
    public void reset() {

    }

    @Override
    public ValidationResult isPlanOpValid(AsgQuery query, CompositePlanOp compositePlanOp, int opIndex) {
        if (opIndex == 0) {
            if (!(compositePlanOp.getOps().get(0) instanceof EntityOp)) {
                return new ValidationResult(
                        false,this.getClass().getSimpleName(),
                        "SingleEntity:Validation failed on:" + compositePlanOp.toString() + "<" + opIndex + ">");
            } else if (compositePlanOp.getOps().size() < 2) {
                return new ValidationResult(
                        false,this.getClass().getSimpleName(),
                        "SingleEntity:Validation failed on:" + compositePlanOp.toString() + "<" + opIndex + ">");
            } else {
                try {
                    if (EEntityBase.class.isAssignableFrom(((EntityOp) compositePlanOp.getOps().get(0)).getAsgEbase().geteBase().getClass())) {
                        return ValidationResult.OK;
                    } else
                        return new ValidationResult(
                                false,this.getClass().getSimpleName(),
                                "SingleEntity:Validation failed on:" + compositePlanOp.toString() + "<" + opIndex + ">");
                } catch (ClassCastException cce) {
                    return new ValidationResult(
                            false,this.getClass().getSimpleName(),
                            "SingleEntity:Validation failed on:" + compositePlanOp.toString() + "<" + opIndex + ">");
                }
            }

        }
        return ValidationResult.OK;
    }

}
