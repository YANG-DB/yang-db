package com.yangdb.fuse.asg.validation;

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

import com.yangdb.fuse.model.asgQuery.AsgEBase;
import com.yangdb.fuse.model.asgQuery.AsgQuery;
import com.yangdb.fuse.model.asgQuery.AsgStrategyContext;
import com.yangdb.fuse.model.ontology.Ontology;
import com.yangdb.fuse.model.ontology.RelationshipType;
import com.yangdb.fuse.model.query.EBase;
import com.yangdb.fuse.model.query.Rel;
import com.yangdb.fuse.model.query.properties.RelProp;
import com.yangdb.fuse.model.query.properties.RelPropGroup;
import com.yangdb.fuse.model.query.properties.constraint.ConstraintOp;
import com.yangdb.fuse.model.validation.ValidationResult;

import java.util.*;
import java.util.stream.Collectors;

import static com.yangdb.fuse.model.asgQuery.AsgQueryUtil.elements;
import static com.yangdb.fuse.model.asgQuery.AsgQueryUtil.nextDescendants;
import static com.yangdb.fuse.model.validation.ValidationResult.OK;
import static com.yangdb.fuse.model.validation.ValidationResult.print;

public class AsgRelPropertiesValidatorStrategy implements AsgValidatorStrategy {

    public static final String ERROR_2 = "Property type mismatch parent Relation ";
    public static final String ERROR_3 = "No %s type found for constraint %s";

    @Override
    public ValidationResult apply(AsgQuery query, AsgStrategyContext context) {
        List<String> errors = new ArrayList<>();
        Ontology.Accessor accessor = context.getOntologyAccessor();
        List<AsgEBase<Rel>> list = nextDescendants(query.getStart(), Rel.class);

        list.forEach(rel -> {
            if(!rel.getB().isEmpty()) {
                AsgEBase<? extends EBase> asgEBase = rel.getB().get(0);
                if(asgEBase.geteBase() instanceof RelProp) {
                    errors.addAll(check(accessor, rel, ((RelProp) asgEBase.geteBase())));
                } else if(asgEBase.geteBase() instanceof RelPropGroup) {
                    errors.addAll(check(accessor, rel, ((RelPropGroup) asgEBase.geteBase())));
                }
            }
        });

        if (errors.isEmpty())
            return OK;

        return new ValidationResult(false, this.getClass().getSimpleName(), errors.toArray(new String[errors.size()]));
    }
    //endregion

    private List<String> check(Ontology.Accessor accessor, AsgEBase<Rel> base, RelPropGroup property) {
        return property.getProps().stream().map(prop -> check(accessor, base, prop))
                .flatMap(Collection::stream).collect(Collectors.toList());
    }

    private List<String> check(Ontology.Accessor accessor, AsgEBase<Rel> base, RelProp property) {
        List<String> errors = new ArrayList<>();
        RelationshipType relationshipType = accessor.$relation$(base.geteBase().getrType());
        String pType = property.getpType();

        if (relationshipType.getProperties().stream().noneMatch(p -> p.equals(pType))) {
            errors.add(ERROR_2 + ":" + print(base, property));
        }

        // if projection type prop -> dont check constraints
        if(property.getProj()!=null) {
            return errors;
        }

        //interval type
        if(property.getCon().getiType()==null) {
            errors.add(String.format(ERROR_3 ," interval type ",property));
        }

        //expresion
        if (!Arrays.asList(ConstraintOp.empty, ConstraintOp.notEmpty).contains(property.getCon().getOp())) {
            if (property.getCon().getExpr() == null) {
                errors.add(String.format(ERROR_3, " expression ", property));
            }
        }

        //operation
        if(property.getCon().getOp()==null) {
            errors.add(String.format(ERROR_3 ," operation ",property));
        }

        return errors;
    }
}
