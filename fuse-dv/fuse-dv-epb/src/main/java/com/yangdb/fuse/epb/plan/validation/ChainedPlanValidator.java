package com.yangdb.fuse.epb.plan.validation;

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

import com.yangdb.fuse.model.validation.ValidationResult;
import com.yangdb.fuse.dispatcher.epb.PlanValidator;
import com.yangdb.fuse.model.asgQuery.AsgQuery;
import com.yangdb.fuse.model.execution.plan.composite.CompositePlanOp;
import com.yangdb.fuse.model.execution.plan.composite.Plan;

/**
 * Created by Roman on 24/04/2017.
 */
public class ChainedPlanValidator implements PlanValidator<Plan, AsgQuery> {

    public interface PlanOpValidator {
        void reset();
        ValidationResult isPlanOpValid(AsgQuery query, CompositePlanOp compositePlanOp, int opIndex);
    }

    //region Constructors
    public ChainedPlanValidator(PlanOpValidator planOpValidator) {
        this.planOpValidator = planOpValidator;
    }
    //endregion

    //region PlanValidator Implementation
    @Override
    public ValidationResult isPlanValid(Plan plan, AsgQuery query) {
        this.planOpValidator.reset();

        for (int opIndex = 0 ; opIndex < plan.getOps().size() ; opIndex++) {
            ValidationResult valid = planOpValidator.isPlanOpValid(query, plan, opIndex);
            if(!valid.valid()) {
                return valid;
            }
        }

        return ValidationResult.OK;
    }
    //endregion

    //region Fields
    private PlanOpValidator planOpValidator;
    //endregion
}


