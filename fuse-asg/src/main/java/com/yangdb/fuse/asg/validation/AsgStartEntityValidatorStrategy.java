package com.yangdb.fuse.asg.validation;

/*-
 * #%L
 * fuse-asg
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



import com.yangdb.fuse.model.asgQuery.AsgEBase;
import com.yangdb.fuse.model.asgQuery.AsgQuery;
import com.yangdb.fuse.model.asgQuery.AsgStrategyContext;
import com.yangdb.fuse.model.ontology.Ontology;
import com.yangdb.fuse.model.query.EBase;
import com.yangdb.fuse.model.query.Start;
import com.yangdb.fuse.model.validation.ValidationResult;

import java.util.List;

import static com.yangdb.fuse.model.asgQuery.AsgQueryUtil.elements;
import static com.yangdb.fuse.model.asgQuery.AsgQueryUtil.nextDescendants;
import static com.yangdb.fuse.model.validation.ValidationResult.OK;

public class AsgStartEntityValidatorStrategy implements AsgValidatorStrategy {

    public static final String ERROR_1 = "Ontology Name doesn't match query Ontology reference";
    public static final String ERROR_2 = "No Elements After Start Node";
    public static final String ERROR_3 = "Start Node must be first element";

    @Override
    public ValidationResult apply(AsgQuery query, AsgStrategyContext context) {
        Ontology.Accessor accessor = context.getOntologyAccessor();

        if (query.getStart().getNext().isEmpty())
            return new ValidationResult(false,this.getClass().getSimpleName(), ERROR_2);
        if (!query.getOnt().equals(accessor.name()))
            return new ValidationResult(false,this.getClass().getSimpleName(), ERROR_1);

        List<AsgEBase<EBase>> list = nextDescendants(query.getStart().getNext().get(0), Start.class);

        if (!list.isEmpty())
            return new ValidationResult(false,this.getClass().getSimpleName(), ERROR_3);


        return OK;
    }
    //endregion
}
