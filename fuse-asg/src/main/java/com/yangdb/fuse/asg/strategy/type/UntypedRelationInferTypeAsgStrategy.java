package com.yangdb.fuse.asg.strategy.type;

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


import com.yangdb.fuse.asg.strategy.AsgStrategy;
import com.yangdb.fuse.model.asgQuery.AsgEBase;
import com.yangdb.fuse.model.asgQuery.AsgQuery;
import com.yangdb.fuse.model.asgQuery.AsgQueryUtil;
import com.yangdb.fuse.model.asgQuery.AsgStrategyContext;
import com.yangdb.fuse.model.ontology.EPair;
import com.yangdb.fuse.model.ontology.RelationshipType;
import com.yangdb.fuse.model.query.EBase;
import com.yangdb.fuse.model.query.Rel;
import com.yangdb.fuse.model.query.RelUntyped;
import com.yangdb.fuse.model.query.entity.EEntityBase;
import com.yangdb.fuse.model.query.entity.ETyped;
import com.yangdb.fuse.model.query.entity.EUntyped;
import com.yangdb.fuse.model.query.quant.QuantType;
import javaslang.collection.Stream;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * for each relation with "*" rel-type (that is Any) replace that 'Any' relation with all 'acceptable'
 * types that match the sideB of the relation - (if sideB is untyped than use as is)
 */
public class UntypedRelationInferTypeAsgStrategy implements AsgStrategy {

    @Override
    public void apply(AsgQuery query, AsgStrategyContext context) {
        Stream.ofAll(AsgQueryUtil.elements(query, RelUntyped.class))
                .forEach(relation -> {
                    Optional<AsgEBase<EBase>> sideA = AsgQueryUtil.ancestor(relation, EEntityBase.class);
                    if (sideA.isPresent()) {
                        //get or generate the Union Quant
                        AsgEBase<? extends EBase> quant = AsgQueryUtil.createOrGetQuant(sideA.get(), query, QuantType.some);
                        //new counter for added path
                        AtomicInteger counter = new AtomicInteger(AsgQueryUtil.max(query));
                        //for each of the Untyped-relation types - add the following path
                        relation.geteBase().getvTypes().forEach(relType -> addRelationPath(counter, context, quant, relType, relation));
                        // remove original untyped relation which is now already duplicated for each rel-types
                        AsgQueryUtil.removePath(query, relation);
                    }
                });
    }

    private void addRelationPath(AtomicInteger counter, AsgStrategyContext context, AsgEBase<? extends EBase> quant, String relType, AsgEBase<RelUntyped> relation) {
        //create clone of the origin unTyped relation
        AsgEBase clone = AsgQueryUtil.deepCloneWithEnums(counter, relation, asgEBase -> true, asgEBase -> true);
        //add clone to union quant
        quant.addNext(clone);
        //change cloned unTyped relation to requeired typed relation
        RelUntyped relUntyped = relation.geteBase();
        clone.seteBase(new Rel(counter.incrementAndGet(), relType, relUntyped.getDir(), String.format("%s.%s", relUntyped.getWrapper(), relType), -1));
        //replace the sideB untyped entity with the appropriate type (or multi-typed) entity
        Set<String> sideBTypes = context.getOntologyAccessor().relation$(relType).getSidesB();
        //change side B entity as infered by this specific relType
        Optional<AsgEBase<EEntityBase>> sideB = AsgQueryUtil.nextAdjacentDescendant(clone, EEntityBase.class);
        //
        if (sideB.isPresent()) {
            AsgEBase<EEntityBase> currentSideB = sideB.get();
            switch (sideBTypes.size()) {
                case 0:
                    //replace to empty untyped
                    currentSideB.seteBase(new EUntyped(currentSideB.geteNum(),
                            String.format("%s.%s", currentSideB.geteBase().geteTag(), relType),
                            currentSideB.geteBase().getNext(), currentSideB.geteBase().getB()));
                    break;
                case 1:
                    //replace to ETyped
                    currentSideB.seteBase(new ETyped(currentSideB.geteNum(),
                            String.format("%s.%s", currentSideB.geteBase().geteTag(), sideBTypes.iterator().next()),
                            sideBTypes.iterator().next(), currentSideB.geteBase().getB()));
                    break;
                default:
                    //replace to multiple types untyped
                    currentSideB.seteBase(new EUntyped(currentSideB.geteNum(),
                            String.format("%s.%s", currentSideB.geteBase().geteTag(), relType),
                            sideBTypes,Collections.emptySet(),
                            currentSideB.geteBase().getNext(), currentSideB.geteBase().getB()));
            }
        }
    }
    //endregion
}
