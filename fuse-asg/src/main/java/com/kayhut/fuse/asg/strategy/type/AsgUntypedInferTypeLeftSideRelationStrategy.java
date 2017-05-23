package com.kayhut.fuse.asg.strategy.type;

import com.kayhut.fuse.asg.strategy.AsgStrategy;
import com.kayhut.fuse.asg.strategy.AsgStrategyContext;
import com.kayhut.fuse.dispatcher.utils.AsgQueryUtil;
import com.kayhut.fuse.model.asgQuery.AsgEBase;
import com.kayhut.fuse.model.asgQuery.AsgQuery;
import com.kayhut.fuse.model.ontology.OntologyUtil;
import com.kayhut.fuse.model.ontology.RelationshipType;
import com.kayhut.fuse.model.query.Rel;
import com.kayhut.fuse.model.query.entity.EUntyped;
import javaslang.collection.Stream;

import java.util.Optional;
import java.util.stream.Collectors;

/**
 * try to infer type for empty list of vTypes in an UnTyped entity
 */
public class AsgUntypedInferTypeLeftSideRelationStrategy implements AsgStrategy {

    @Override
    public void apply(AsgQuery query, AsgStrategyContext context) {
        Stream.ofAll(AsgQueryUtil.<EUntyped>elements(query, EUntyped.class))
                .forEach(asgEBase -> {
                    Optional<AsgEBase<Rel>> relation = AsgQueryUtil.nextAdjacentDescendant(asgEBase, Rel.class);
                    if(relation.isPresent()) {
                        AsgEBase<Rel> rel = relation.get();
                        Optional<RelationshipType> relationshipType = OntologyUtil.getRelationshipType(context.getOntology(), rel.geteBase().getrType());
                        asgEBase.geteBase().setvTypes(relationshipType.get().getePairs().stream().map(p -> p.geteTypeA()).collect(Collectors.toList()));
                    }
                });

    }
    //endregion
}
