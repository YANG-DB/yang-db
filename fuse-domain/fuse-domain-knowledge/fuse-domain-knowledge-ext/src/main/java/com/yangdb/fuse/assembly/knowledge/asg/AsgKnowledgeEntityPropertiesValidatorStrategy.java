package com.yangdb.fuse.assembly.knowledge.asg;

/*-
 *
 * fuse-domain-knowledge-ext
 * %%
 * Copyright (C) 2016 - 2019 The Fuse Graph Database Project
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

import com.yangdb.fuse.asg.validation.AsgEntityPropertiesValidatorStrategy;
import com.yangdb.fuse.model.asgQuery.AsgEBase;
import com.yangdb.fuse.model.asgQuery.AsgQuery;
import com.yangdb.fuse.model.asgQuery.AsgStrategyContext;
import com.yangdb.fuse.model.ontology.EntityType;
import com.yangdb.fuse.model.ontology.Ontology;
import com.yangdb.fuse.model.query.entity.EEntityBase;
import com.yangdb.fuse.model.query.entity.EUntyped;
import com.yangdb.fuse.model.query.entity.Typed;
import com.yangdb.fuse.model.query.properties.EProp;
import com.yangdb.fuse.model.query.properties.constraint.ConstraintOp;
import com.yangdb.fuse.model.validation.ValidationResult;
import javaslang.collection.Stream;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.yangdb.fuse.model.query.properties.constraint.ConstraintOp.ignorableConstraints;
import static com.yangdb.fuse.model.validation.ValidationResult.print;

public class AsgKnowledgeEntityPropertiesValidatorStrategy extends AsgEntityPropertiesValidatorStrategy {

    private final String parentPtype;
    private final String childPtype;

    public AsgKnowledgeEntityPropertiesValidatorStrategy(String parentPtype, String childPtype) {
        this.parentPtype = parentPtype;
        this.childPtype = childPtype;
    }

    @Override
    public ValidationResult apply(AsgQuery query, AsgStrategyContext context) {
        return super.apply(query, context);
    }

    protected List<String> check(Ontology.Accessor accessor, AsgEBase<EEntityBase> base, EProp property) {
        List<String> errors = new ArrayList<>();
        if (base.geteBase() instanceof Typed.eTyped) {
            EntityType entityType = accessor.$entity$(((Typed.eTyped) base.geteBase()).geteType());
            String pType = property.getpType();

            //if pType is composite -> explicitly take the schematic name in the second place : fName.stringValue
            if(pType.contains(".")) {
                pType = pType.split("[.]")[1];
            }

            //if eType is of parent - check is that child also contains the property
            String finalPType = pType;
            if (entityType.geteType().equals(parentPtype)) {
                if (entityType.getProperties().stream().noneMatch(p -> p.equals(finalPType)) &&
                        accessor.$entity$(childPtype).getProperties().stream().noneMatch(p -> p.equals(finalPType))) {
                    errors.add(ERROR_2 + ":" + print(base, property));
                }
            } else {
                //regular fallback check
                if (entityType.getProperties().stream().noneMatch(p -> p.equals(finalPType))) {
                    errors.add(ERROR_2 + ":" + print(base, property));
                }
            }

        } else if (base.geteBase() instanceof EUntyped) {
            Stream<String> types = Stream.ofAll(((EUntyped) base.geteBase()).getvTypes()).map(accessor::$entity$).flatMap(EntityType::getProperties);
            String pType = property.getpType();

            //skip projection fields validation
            if (property.getProj() == null) {
                if (types.toJavaStream().noneMatch(p -> p.equals(pType))) {
                    errors.add(ERROR_2 + ":" + print(base, property));
                }
            }
        }

        // if projection type prop -> dont check constraints
        if (property.getProj() != null) {
            return errors;
        }

        if (ignorableConstraints.contains(property.getCon().getClass()))
            return errors;

        //interval type
        if (property.getCon().getiType() == null) {
            errors.add(String.format(ERROR_3, " interval type ", property));
        }

        //expresion
        if (!Arrays.asList(ConstraintOp.empty, ConstraintOp.notEmpty).contains(property.getCon().getOp())) {
            if (property.getCon().getExpr() == null) {
                errors.add(String.format(ERROR_3, " expression ", property));
            }
        }

        //operation
        if (property.getCon().getOp() == null) {
            errors.add(String.format(ERROR_3, " operation ", property));
        }
        return errors;
    }
}
