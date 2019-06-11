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

import com.kayhut.fuse.model.asgQuery.AsgQuery;
import com.kayhut.fuse.model.asgQuery.AsgQueryUtil;
import com.kayhut.fuse.model.asgQuery.AsgStrategyContext;
import com.kayhut.fuse.model.ontology.Ontology;
import com.kayhut.fuse.model.query.properties.EProp;
import com.kayhut.fuse.model.query.properties.constraint.WhereByConstraint;
import com.kayhut.fuse.model.validation.ValidationResult;
import javaslang.collection.Stream;

import java.util.ArrayList;
import java.util.List;

import static com.kayhut.fuse.model.validation.ValidationResult.OK;

public class AsgWhereByConstraintValidatorStrategy implements AsgValidatorStrategy {

    public static final String ERROR_2 = "where by constraint projection name doesnt match any existing tag ";
    public static final String ERROR_3 = "where by constraint field doesnt match entity ontological field";

    @Override
    public ValidationResult apply(AsgQuery query, AsgStrategyContext context) {
        List<String> errors = new ArrayList<>();
        //todo - add validation on where by clause
        // - verify tags correlate
        // - verify field exists on tagged entity in the ontology
        Ontology.Accessor accessor = context.getOntologyAccessor();
        int count = Stream.ofAll(AsgQueryUtil.elements(query, EProp.class)).count(p -> isWhereClause(p.geteBase()));

        if(count > 1) {
            errors.add(ERROR_2);
        }

        if (errors.isEmpty())
            return OK;

        return new ValidationResult(false, this.getClass().getSimpleName(), errors.toArray(new String[errors.size()]));
    }
    //endregion



    private boolean isWhereClause(EProp p) {
        return p.getCon()!=null && p.getCon() instanceof WhereByConstraint;
    }
}
