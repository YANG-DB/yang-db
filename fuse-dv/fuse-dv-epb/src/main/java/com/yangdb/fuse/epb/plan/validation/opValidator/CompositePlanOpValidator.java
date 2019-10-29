package com.yangdb.fuse.epb.plan.validation.opValidator;

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

import com.yangdb.fuse.model.validation.ValidationResult;
import com.yangdb.fuse.epb.plan.validation.ChainedPlanValidator;
import com.yangdb.fuse.model.asgQuery.AsgQuery;
import com.yangdb.fuse.model.execution.plan.composite.CompositePlanOp;
import javaslang.collection.Stream;

import java.util.List;

/**
 * Created by Roman on 24/04/2017.
 */
public class CompositePlanOpValidator implements ChainedPlanValidator.PlanOpValidator {
    public enum Mode {
        one,
        all
    }

    //region Constructors
    public CompositePlanOpValidator(Mode mode, ChainedPlanValidator.PlanOpValidator...planOpValidators) {
        this.mode = mode;
        this.planOpValidators = Stream.of(planOpValidators).toJavaList();
    }

    public CompositePlanOpValidator(Mode mode, Iterable<ChainedPlanValidator.PlanOpValidator> planOpValidators) {
        this.mode = mode;
        this.planOpValidators = Stream.ofAll(planOpValidators).toJavaList();
    }
    //endregion

    //region Public Method
    public CompositePlanOpValidator with(ChainedPlanValidator.PlanOpValidator planOpValidator) {
        this.planOpValidators.add(planOpValidator);
        return this;
    }
    //endregion

    //region ChainedPlanValidator.PlanOpValidator Implementation
    @Override
    public void reset() {
        this.planOpValidators.forEach(ChainedPlanValidator.PlanOpValidator::reset);
    }

    @Override
    public ValidationResult isPlanOpValid(AsgQuery query, CompositePlanOp compositePlanOp, int opIndex) {
        for(ChainedPlanValidator.PlanOpValidator planOpValidator : this.planOpValidators) {
            ValidationResult planOpValid = planOpValidator.isPlanOpValid(query, compositePlanOp, opIndex);

            if (planOpValid.valid() && this.mode == Mode.one) {
                return ValidationResult.OK;
            }

            if (!planOpValid.valid() && this.mode == Mode.all) {
                return planOpValid;
            }
        }

        if(this.mode == Mode.all) {
            return ValidationResult.OK;
        }

        return new ValidationResult(false,this.getClass().getSimpleName(), "Not all valid");
    }
    //endregion

    //region Fields
    private List<ChainedPlanValidator.PlanOpValidator> planOpValidators;
    private Mode mode;
    //endregion
}
