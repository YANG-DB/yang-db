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
import com.kayhut.fuse.model.ontology.Property;
import com.kayhut.fuse.model.query.properties.EProp;
import com.kayhut.fuse.model.query.properties.constraint.Constraint;
import com.kayhut.fuse.model.query.properties.constraint.InnerQueryConstraint;
import com.kayhut.fuse.model.query.properties.constraint.WhereByConstraint;
import com.kayhut.fuse.model.query.properties.constraint.WhereByFacet;
import com.kayhut.fuse.model.validation.ValidationResult;
import javaslang.collection.Stream;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.kayhut.fuse.model.validation.ValidationResult.OK;

public class AsgWhereByConstraintValidatorStrategy implements AsgValidatorStrategy {

    public static final String ERROR_1 = "Only single where by constraint can exist in a query";
    public static final String ERROR_2 = "where by constraint projection name %s doesnt match any existing tag ";
    public static final String ERROR_3 = "where by constraint field doesnt %s match entity ontological field";

    @Override
    public ValidationResult apply(AsgQuery query, AsgStrategyContext context) {
        List<String> errors = new ArrayList<>();
        //todo - add validation on where by clause
        // - verify tags correlate
        // - verify field exists on tagged entity in the ontology
        int count = Stream.ofAll(AsgQueryUtil.getEprops(query)).count(this::isWhereClause);

        if(count > 1) {
            errors.add(ERROR_1);
        }

        Stream.ofAll(AsgQueryUtil.getEprops(query))
                .filter(this::isWhereClause)
                .forEach(p->{
                    WhereByFacet con = (WhereByFacet) p.getCon();
                    String tagEntity = con.getTagEntity();
                    if(!AsgQueryUtil.getByTag(query.getStart(),tagEntity).isPresent())
                        errors.add(String.format(ERROR_2,tagEntity));

                    String projectedField = con.getProjectedField();
                    if(!context.getOntologyAccessor().$property(projectedField).isPresent())
                        errors.add(String.format(ERROR_3,projectedField));

                });

        if (errors.isEmpty())
            return OK;

        return new ValidationResult(false, this.getClass().getSimpleName(), errors.toArray(new String[errors.size()]));
    }
    //endregion



    private boolean isWhereClause(EProp p) {
        return p.getCon()!=null && p.getCon() instanceof WhereByFacet;
    }

}
