package com.yangdb.fuse.asg.strategy.type;

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

import com.yangdb.fuse.asg.strategy.AsgStrategy;
import com.yangdb.fuse.model.asgQuery.AsgStrategyContext;
import com.yangdb.fuse.model.asgQuery.AsgQueryUtil;
import com.yangdb.fuse.model.asgQuery.AsgEBase;
import com.yangdb.fuse.model.asgQuery.AsgQuery;
import com.yangdb.fuse.model.ontology.EPair;
import com.yangdb.fuse.model.ontology.RelationshipType;
import com.yangdb.fuse.model.query.Rel;
import com.yangdb.fuse.model.query.entity.EUntyped;
import javaslang.collection.Stream;

import java.util.*;
import java.util.stream.Collectors;

/**
 * try to infer type for empty list of vTypes in an UnTyped entity
 */
public class UntypedInferTypeLeftSideRelationAsgStrategy implements AsgStrategy {

    @Override
    public void apply(AsgQuery query, AsgStrategyContext context) {
        Stream.ofAll(AsgQueryUtil.elements(query, EUntyped.class))
                .forEach(sideA -> {
                    Optional<AsgEBase<Rel>> relation = AsgQueryUtil.nextAdjacentDescendant(sideA, Rel.class);
                    if(relation.isPresent()) {
                        AsgEBase<Rel> rel = relation.get();
                        Optional<RelationshipType> relationshipType = context.getOntologyAccessor().$relation(rel.geteBase().getrType());
                        ArrayList<String> sideAvTypes = new ArrayList<>(relationshipType.get().getePairs().stream().map(EPair::geteTypeA).collect(Collectors.groupingBy(v -> v, Collectors.toSet())).keySet());

                        //try populating side B of the rel is it is an Untyped
                        Optional<AsgEBase<EUntyped>> sideB = AsgQueryUtil.nextAdjacentDescendant(rel, EUntyped.class);
                        if(sideB.isPresent()) {
                            ArrayList<String> sideBvTypes = new ArrayList<>(relationshipType.get().getePairs().stream().map(EPair::geteTypeB).collect(Collectors.groupingBy(v -> v, Collectors.toSet())).keySet());
                            //populate possible types only if no types present on entity
                            if(sideB.get().geteBase().getvTypes().isEmpty()) {
                                sideB.get().geteBase().getvTypes().addAll(sideBvTypes);
                            }
                        }
                        //populate possible types only if no types present on entity
                        if(sideA.geteBase().getvTypes().isEmpty()) {
                            sideA.geteBase().getvTypes().addAll(sideAvTypes);
                        }
                    }
                });

    }
    //endregion
}
