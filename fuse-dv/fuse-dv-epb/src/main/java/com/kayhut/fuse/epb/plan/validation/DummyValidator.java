package com.kayhut.fuse.epb.plan.validation;

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

import com.kayhut.fuse.model.validation.ValidationResult;
import com.kayhut.fuse.dispatcher.epb.PlanValidator;

/**
 * Created by moti on 2/23/2017.
 */
public class DummyValidator<P,Q> implements PlanValidator<P,Q> {
    @Override
    public ValidationResult isPlanValid(P plan, Q query) {
        return ValidationResult.OK;
    }
}
