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

import com.yangdb.fuse.model.asgQuery.AsgQueryUtil;
import com.yangdb.fuse.model.asgQuery.AsgEBase;
import com.yangdb.fuse.model.asgQuery.AsgQuery;
import com.yangdb.fuse.model.asgQuery.AsgStrategyContext;
import com.yangdb.fuse.model.ontology.EntityType;
import com.yangdb.fuse.model.ontology.Ontology;
import com.yangdb.fuse.model.query.EBase;
import com.yangdb.fuse.model.query.entity.EEntityBase;
import com.yangdb.fuse.model.query.entity.EUntyped;
import com.yangdb.fuse.model.query.entity.Typed;
import com.yangdb.fuse.model.query.properties.CalculatedEProp;
import com.yangdb.fuse.model.query.properties.EProp;
import com.yangdb.fuse.model.query.properties.EPropGroup;
import com.yangdb.fuse.model.query.properties.constraint.ConstraintOp;
import com.yangdb.fuse.model.query.quant.QuantBase;
import com.yangdb.fuse.model.validation.ValidationResult;
import javaslang.collection.Stream;

import java.util.*;
import java.util.stream.Collectors;

import static com.yangdb.fuse.model.query.properties.constraint.ConstraintOp.ignorableConstraints;
import static com.yangdb.fuse.model.validation.ValidationResult.OK;
import static com.yangdb.fuse.model.validation.ValidationResult.print;

public class AsgEntityPropertiesValidatorStrategy implements AsgValidatorStrategy {

    public static final String ERROR_1 = "No Parent Element found  ";
    public static final String ERROR_2 = "Property type mismatch parent entity";
    public static final String ERROR_3 = "No %s type found for constraint %s";

    @Override
    public ValidationResult apply(AsgQuery query, AsgStrategyContext context) {
        List<String> errors = new ArrayList<>();

        Ontology.Accessor accessor = context.getOntologyAccessor();
        Stream.ofAll(AsgQueryUtil.elements(query, EProp.class))
                .filter(property -> !(property.geteBase() instanceof CalculatedEProp))
                .forEach(property -> {
                    Optional<AsgEBase<EEntityBase>> parent = calculateNextAncestor(property,EEntityBase.class);
                    if (!parent.isPresent()) {
                        errors.add(ERROR_1 + ":" + property);
                    } else {
                        errors.addAll(check(accessor, parent.get(), property.geteBase()));
                    }
                });

        Stream.ofAll(AsgQueryUtil.elements(query, EPropGroup.class))
                .forEach(group -> {
                    Optional<AsgEBase<EEntityBase>> parent = calculateNextAncestor(group,EEntityBase.class);
                    if (!parent.isPresent()) {
                        errors.add(ERROR_1 + group);
                    } else {
                        errors.addAll(check(accessor, parent.get(), group.geteBase()));
                    }
                });
        if (errors.isEmpty())
            return OK;

        return new ValidationResult(false, this.getClass().getSimpleName(), errors.toArray(new String[errors.size()]));
    }
    //endregion

    private List<String> check(Ontology.Accessor accessor, AsgEBase<EEntityBase> base, EPropGroup property) {
        return property.getProps().stream()
                .filter(prop->!CalculatedEProp.class.isAssignableFrom(prop.getClass()))
                .map(prop->check(accessor,base,prop))
                .flatMap(Collection::stream).collect(Collectors.toList());
    }

    private List<String> check(Ontology.Accessor accessor, AsgEBase<EEntityBase> base, EProp property) {
        List<String> errors = new ArrayList<>();
        if (base.geteBase() instanceof Typed.eTyped) {
            EntityType entityType = accessor.$entity$(((Typed.eTyped) base.geteBase()).geteType());
            String pType = property.getpType();

            if (entityType.getProperties().stream().noneMatch(p -> p.equals(pType))) {
                errors.add(ERROR_2 + ":" + print(base, property));
            }

        } else if (base.geteBase() instanceof EUntyped) {
            Stream<String> types = Stream.ofAll(((EUntyped) base.geteBase()).getvTypes()).map(accessor::$entity$).flatMap(EntityType::getProperties);
            String pType = property.getpType();

            //skip projection fields validation
            if(property.getProj()==null) {
                if (types.toJavaStream().noneMatch(p -> p.equals(pType))) {
                    errors.add(ERROR_2 + ":" + print(base, property));
                }
            }
        }

        // if projection type prop -> dont check constraints
        if(property.getProj()!=null) {
            return errors;
        }

        if(ignorableConstraints.contains(property.getCon().getClass()))
            return errors;

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

    public static  <T extends EBase> Optional<AsgEBase<T>> calculateNextAncestor(AsgEBase<? extends EBase> eProp, Class<T> clazz) {
        final List<AsgEBase<? extends EBase>> path = AsgQueryUtil.pathToAncestor(eProp, clazz);
        Optional<AsgEBase<T>> element = Optional.empty();
        if(!path.isEmpty() && path.size()==2)
            element = Optional.of((AsgEBase<T>) path.get(1));
        if(!path.isEmpty() && path.size()==3 && QuantBase.class.isAssignableFrom(path.get(1).geteBase().getClass()))
            element = Optional.of((AsgEBase<T>) path.get(2));
        return element;
    }
}
