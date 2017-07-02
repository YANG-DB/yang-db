package com.kayhut.fuse.neo4j.cypher.strategy;

import com.kayhut.fuse.model.asgQuery.AsgEBase;
import com.kayhut.fuse.model.ontology.Ontology;
import com.kayhut.fuse.model.ontology.RelationshipType;
import com.kayhut.fuse.model.query.Rel;
import com.kayhut.fuse.neo4j.cypher.CypherCompilationState;
import com.kayhut.fuse.neo4j.cypher.CypherRelationship;
import com.kayhut.fuse.neo4j.cypher.CypherReturnElement;

import java.util.Map;
import java.util.Optional;

/**
 * Created by User on 26/03/2017.
 */
public class TypedRelCypherStrategy extends CypherStrategy {

    public TypedRelCypherStrategy(Map<AsgEBase, CypherCompilationState> compilationState, Ontology ont) {
        super(compilationState, ont);
    }

    public CypherCompilationState apply(AsgEBase element) {

        if (element.geteBase() instanceof Rel) {

            Rel typedRel = (Rel) element.geteBase();

            CypherCompilationState curState = getRelevantState(element);

            Optional<RelationshipType> relation =  new Ontology.Accessor(this.ontology).$relation(typedRel.getrType());

            if (!relation.isPresent()) {
                throw new RuntimeException("Failed compiling query. Unknown Relationship type: " + typedRel.getrType());
            }

            //TODO: v1 doesn't have tags for relationships.
            String tag = curState.getStatement().getNewRelTag();

            CypherRelationship rel = CypherRelationship.cypherRel()
                                                       .withLabel(relation.get().getName())
                                                       .withTag(tag)
                                                       .withDirection(getDirection(typedRel));

            CypherReturnElement returnElement = CypherReturnElement.cypherReturnElement().withTag(rel.tag);

            //create updated state with new statement
            return context(element, new CypherCompilationState(curState.getStatement()
                                                                    .appendRel(curState.getPathTag(), rel)
                                                                    .addReturn(returnElement),
                                                                curState.getPathTag()));
        }

        return getRelevantState(element);
    }

    private CypherRelationship.Direction getDirection(Rel typedRel) {
        if(typedRel.getDir() == null) {
            return CypherRelationship.Direction.BOTH;
        }
        if(typedRel.getDir().equals(Rel.Direction.R)) {
            return CypherRelationship.Direction.RIGHT;
        } else if(typedRel.getDir().equals(Rel.Direction.L)){
            return CypherRelationship.Direction.LEFT;
        } else {
            return CypherRelationship.Direction.BOTH;
        }
    }
}

