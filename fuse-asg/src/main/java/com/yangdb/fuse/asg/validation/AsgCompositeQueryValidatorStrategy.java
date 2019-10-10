package com.yangdb.fuse.asg.validation;

/*-
 *
 * fuse-asg
 * %%
 * Copyright (C) 2016 - 2019 yangdb   ------ www.yangdb.org ------
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
 *
 */

import com.yangdb.fuse.model.asgQuery.AsgQuery;
import com.yangdb.fuse.model.asgQuery.AsgStrategyContext;
import com.yangdb.fuse.model.validation.ValidationResult;

import java.util.ArrayList;
import java.util.List;

import static com.yangdb.fuse.model.asgQuery.AsgCompositeQuery.isComposite;
import static com.yangdb.fuse.model.validation.ValidationResult.OK;

public class AsgCompositeQueryValidatorStrategy implements AsgValidatorStrategy {

    public static final String ERROR_1 = "Ontology Contains Cycle ";

    @Override
    public ValidationResult apply(AsgQuery query, AsgStrategyContext context) {
        List<String> errors = new ArrayList<>();

        if(isComposite(query)) {
            //todo - validate composite query
//            1) check hierarchy level is limited to 1
//            2) check a parameterized constraint exists
//            3) check inner query constraint tag reference is valid

        }

        if (errors.isEmpty())
            return OK;

        return new ValidationResult(false, this.getClass().getSimpleName(), errors.toArray(new String[errors.size()]));
    }
    //endregion
}
