package com.kayhut.fuse.asg.strategy.type;

import com.kayhut.fuse.asg.strategy.AsgStrategy;
import com.kayhut.fuse.asg.strategy.AsgStrategyContext;
import com.kayhut.fuse.dispatcher.utils.AsgQueryUtil;
import com.kayhut.fuse.model.asgQuery.AsgEBase;
import com.kayhut.fuse.model.asgQuery.AsgQuery;
import com.kayhut.fuse.model.ontology.EPair;
import com.kayhut.fuse.model.ontology.OntologyUtil;
import com.kayhut.fuse.model.ontology.RelationshipType;
import com.kayhut.fuse.model.query.EBase;
import com.kayhut.fuse.model.query.Rel;
import com.kayhut.fuse.model.query.entity.EUntyped;
import javaslang.collection.Stream;

import java.util.*;
import java.util.stream.Collectors;

/**
 * try to infer type for empty list of vTypes in an UnTyped entity
 */
public class AsgUntypedInferTypeLeftSideRelationStrategy implements AsgStrategy {

    @Override
    public void apply(AsgQuery query, AsgStrategyContext context) {
        Stream.ofAll(AsgQueryUtil.elements(query, EUntyped.class))
                .forEach(sideA -> {
                    Optional<AsgEBase<Rel>> relation = AsgQueryUtil.nextAdjacentDescendant(sideA, Rel.class);
                    if(relation.isPresent()) {
                        AsgEBase<Rel> rel = relation.get();
                        Optional<RelationshipType> relationshipType = OntologyUtil.getRelationshipType(context.getOntology(), rel.geteBase().getrType());
                        ArrayList<Integer> sideAvTypes = new ArrayList<>(relationshipType.get().getePairs().stream().map(EPair::geteTypeA).collect(Collectors.groupingBy(v -> v, Collectors.toSet())).keySet());

                        //try populating side B of the rel is it is an Untyped
                        Optional<AsgEBase<EUntyped>> sideB = AsgQueryUtil.nextAdjacentDescendant(rel, EUntyped.class);
                        if(sideB.isPresent()) {
                            ArrayList<Integer> sideBvTypes = new ArrayList<>(relationshipType.get().getePairs().stream().map(EPair::geteTypeB).collect(Collectors.groupingBy(v -> v, Collectors.toSet())).keySet());
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
