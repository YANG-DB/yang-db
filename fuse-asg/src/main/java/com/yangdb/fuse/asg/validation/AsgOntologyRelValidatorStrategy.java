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
import com.yangdb.fuse.model.ontology.EPair;
import com.yangdb.fuse.model.ontology.Ontology;
import com.yangdb.fuse.model.query.EBase;
import com.yangdb.fuse.model.query.Rel;
import com.yangdb.fuse.model.query.entity.EEntityBase;
import com.yangdb.fuse.model.query.entity.EUntyped;
import com.yangdb.fuse.model.query.entity.Typed;
import com.yangdb.fuse.model.query.quant.QuantBase;
import com.yangdb.fuse.model.validation.ValidationResult;
import javaslang.collection.HashSet;
import javaslang.collection.Set;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.yangdb.fuse.model.asgQuery.AsgQueryUtil.elements;
import static com.yangdb.fuse.model.asgQuery.AsgQueryUtil.nextDescendants;
import static com.yangdb.fuse.model.validation.ValidationResult.OK;

public class AsgOntologyRelValidatorStrategy implements AsgValidatorStrategy {
    public static final String ERROR_1 = "Ontology doesn't Allow Relation with No entity Attached to ";
    public static final String ERROR_2 = "Ontology doesn't Allow such Relation with Entities construct ";

    @Override
    public ValidationResult apply(AsgQuery query, AsgStrategyContext context) {
        List<String> errors = new ArrayList<>();
        Ontology.Accessor accessor = context.getOntologyAccessor();

        List<AsgEBase<Rel>> list = nextDescendants(query.getStart(), Rel.class);

        list.forEach(rel -> {
            Rel.Direction dir = rel.geteBase().getDir();

            Optional<AsgEBase<EEntityBase>> sideA = AsgQueryUtil.ancestor(rel, v -> v.geteBase() instanceof EEntityBase);
            Optional<AsgEBase<EEntityBase>> sideB = calculateNextDescendant(rel,EEntityBase.class);

            if (dir.equals(Rel.Direction.L)) {
                sideB = AsgQueryUtil.ancestor(rel, v -> v.geteBase() instanceof EEntityBase);
                sideA = calculateNextDescendant(rel,EEntityBase.class);
            }

            if (!sideA.isPresent() || !sideB.isPresent())
                errors.add(ERROR_1 + ":" + rel);

            List<EPair> elements = accessor.$relation$(rel.geteBase().getrType()).getePairs();
            Set<String> allowedSideA = HashSet.<String>of().addAll(elements.stream().map(EPair::geteTypeA).collect(Collectors.toList()));
            Set<String> allowedSideB = HashSet.<String>of().addAll(elements.stream().map(EPair::geteTypeB).collect(Collectors.toList()));

            Set<String> sideATypes = getSideTypes(sideA);
            Set<String> sideBTypes = getSideTypes(sideB);


            if (!sideATypes.isEmpty()) {
                if(allowedSideA.intersect(sideATypes).size() < sideATypes.size())
                    errors.add(ERROR_2 + ":" + ValidationResult.print(sideA.get(), rel, sideB.get()));
            }

            if (!sideBTypes.isEmpty()) {
                if(allowedSideB.intersect(sideBTypes).size() < sideBTypes.size())
                    errors.add(ERROR_2 + ":" + ValidationResult.print(sideA.get(), rel, sideB.get()));
            }
        });

        if (errors.isEmpty())
            return OK;

        return new ValidationResult(false, this.getClass().getSimpleName(), errors.toArray(new String[errors.size()]));
    }

    private <T extends EBase> Optional<AsgEBase<T>> calculateNextDescendant(AsgEBase<Rel> rel, Class<T> clazz) {
        final List<AsgEBase<? extends EBase>> path = AsgQueryUtil.pathToNextDescendant(rel, clazz);
        Optional<AsgEBase<T>> element = Optional.empty();
        if(!path.isEmpty() && path.size()==2)
            element = Optional.of((AsgEBase<T>) path.get(1));
        if(!path.isEmpty() && path.size()==3 && QuantBase.class.isAssignableFrom(path.get(1).geteBase().getClass()))
            element = Optional.of((AsgEBase<T>) path.get(2));
        return element;
    }

    private Set<String> getSideTypes(Optional<AsgEBase<EEntityBase>> side) {
        Set<String> hashSet = HashSet.of();
        if (side.isPresent()) {
            if (Typed.eTyped.class.isAssignableFrom(side.get().geteBase().getClass())) {
                hashSet = hashSet.add(((Typed.eTyped) side.get().geteBase()).geteType());
            } else if (EUntyped.class.isAssignableFrom(side.get().geteBase().getClass())) {
                hashSet = hashSet.addAll(((EUntyped) side.get().geteBase()).getvTypes());
            }
        }
        return hashSet;
    }
    //endregion
}
