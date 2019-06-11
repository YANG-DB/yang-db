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
import com.kayhut.fuse.model.ontology.Ontology;
import com.kayhut.fuse.model.ontology.RelationshipType;
import com.kayhut.fuse.model.query.EBase;
import com.kayhut.fuse.model.query.Rel;
import com.kayhut.fuse.model.query.RelPattern;
import com.kayhut.fuse.model.query.properties.RelProp;
import com.kayhut.fuse.model.query.properties.RelPropGroup;
import com.kayhut.fuse.model.query.properties.constraint.ConstraintOp;
import com.kayhut.fuse.model.validation.ValidationResult;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import static com.kayhut.fuse.model.asgQuery.AsgQueryUtil.nextDescendants;
import static com.kayhut.fuse.model.validation.ValidationResult.OK;
import static com.kayhut.fuse.model.validation.ValidationResult.print;

/**
 * check that the content of the relPattern is valid in terms of range and that the
 * pattern that appears after the relPattern itself has a single exit point so that the
 */
public class AsgRelPatternValidatorStrategy implements AsgValidatorStrategy {

    public static final String ERROR_2 = "Property type mismatch parent Relation ";

    @Override
    public ValidationResult apply(AsgQuery query, AsgStrategyContext context) {
        List<String> errors = new ArrayList<>();
        Ontology.Accessor accessor = context.getOntologyAccessor();
        List<AsgEBase<RelPattern>> list = nextDescendants(query.getStart(), RelPattern.class);

        list.forEach(rel -> {
            //todo implement:
            //      - test range is legal
            //      - test that tag exists it has the eval pattern *$:{}
            //      - test that if a EndPattern exists - only a single exit point from the pattern (RelPattern - ..... - EndPattern)

        });

        if (errors.isEmpty())
            return OK;

        return new ValidationResult(false, this.getClass().getSimpleName(), errors.toArray(new String[errors.size()]));
    }
    //endregion

}
