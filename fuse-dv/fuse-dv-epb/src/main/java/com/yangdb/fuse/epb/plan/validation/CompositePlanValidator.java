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
import javaslang.collection.Stream;

/**
 * Created by Roman on 30/04/2017.
 */
public class CompositePlanValidator<P, Q> implements PlanValidator<P, Q> {
    public enum Mode {
        one,
        all
    }

    //region Constructors
    public CompositePlanValidator(Mode mode, PlanValidator<P, Q>...validators) {
        this.mode = mode;
        this.validators = Stream.of(validators).toJavaList();
    }

    public CompositePlanValidator(Mode mode, Iterable<PlanValidator<P, Q>> validators) {
        this.mode = mode;
        this.validators = Stream.ofAll(validators).toJavaList();
    }
    //endregion

    //region PlanValidator Implementation
    @Override
    public ValidationResult isPlanValid(P plan, Q query) {
        for(PlanValidator<P, Q> validator : this.validators) {
            ValidationResult planValid = validator.isPlanValid(plan, query);

            if (planValid.valid() && this.mode == Mode.one) {
                return ValidationResult.OK;
            }

            if (!planValid.valid() && this.mode == Mode.all) {
                return planValid;
            }
        }

        if(this.mode == Mode.all) {
            return ValidationResult.OK;
        }

        return new ValidationResult(false,this.getClass().getSimpleName(),"Not all valid");
    }
    //endregion

    //region Fields
    protected Mode mode;
    protected Iterable<PlanValidator<P, Q>> validators;
    //endregion
}
