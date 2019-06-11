package com.kayhut.fuse.asg.validation;

/*-
 * #%L
 * fuse-asg
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

import com.kayhut.fuse.model.asgQuery.AsgEBase;
import com.kayhut.fuse.model.asgQuery.AsgQuery;
import com.kayhut.fuse.model.asgQuery.AsgStrategyContext;
import com.kayhut.fuse.model.query.EBase;
import com.kayhut.fuse.model.validation.ValidationResult;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.kayhut.fuse.model.asgQuery.AsgQueryUtil.elements;
import static com.kayhut.fuse.model.validation.ValidationResult.OK;

public class AsgCycleValidatorStrategy implements AsgValidatorStrategy {

    public static final String ERROR_1 = "Ontology Contains Cycle ";

    @Override
    public ValidationResult apply(AsgQuery query, AsgStrategyContext context) {
        List<String> errors = new ArrayList<>();
        List<AsgEBase<EBase>> elements = elements(query);
        if(new java.util.HashSet<>(elements).size() < elements.size())
            errors.add(ERROR_1);


        if (errors.isEmpty())
            return OK;

        return new ValidationResult(false, this.getClass().getSimpleName(), errors.toArray(new String[errors.size()]));
    }
    //endregion
}
