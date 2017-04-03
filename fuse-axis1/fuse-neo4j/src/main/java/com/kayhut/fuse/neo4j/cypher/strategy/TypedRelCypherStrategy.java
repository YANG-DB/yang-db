package com.kayhut.fuse.neo4j.cypher.strategy;

import com.kayhut.fuse.model.asgQuery.AsgEBase;
import com.kayhut.fuse.model.ontology.Ontology;
import com.kayhut.fuse.model.ontology.OntologyUtil;
import com.kayhut.fuse.model.ontology.Property;
import com.kayhut.fuse.model.query.Rel;
import com.kayhut.fuse.model.query.aggregation.AggL1;
import com.kayhut.fuse.model.query.properties.RelProp;
import com.kayhut.fuse.neo4j.cypher.CypherCompilationState;
import com.kayhut.fuse.neo4j.cypher.CypherCondition;
import com.kayhut.fuse.neo4j.cypher.CypherRelationship;
import com.kayhut.fuse.neo4j.cypher.CypherReturnElement;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.kayhut.fuse.neo4j.cypher.CypherOps.getOp;

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

            Optional<String> label = OntologyUtil.getRelationLabel(ontology, typedRel.getrType());

            if (!label.isPresent()) {
                throw new RuntimeException("Failed compiling query. Unknown entity type: " + typedRel.getrType());
            }

            //TODO: v1 doesn't have tags for relationships.
            String tag = curState.getStatement().getNewRelTag();

            CypherRelationship rel = CypherRelationship.cypherRel()
                                                       .withLabel(label.get())
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
        if(typedRel.getDir().equals("R")) {
            return CypherRelationship.Direction.RIGHT;
        } else if(typedRel.getDir().equals("L")){
            return CypherRelationship.Direction.LEFT;
        } else {
            return CypherRelationship.Direction.BOTH;
        }
    }
}

