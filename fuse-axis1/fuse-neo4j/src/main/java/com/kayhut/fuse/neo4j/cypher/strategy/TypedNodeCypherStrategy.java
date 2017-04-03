package com.kayhut.fuse.neo4j.cypher.strategy;

import com.kayhut.fuse.model.asgQuery.AsgEBase;
import com.kayhut.fuse.model.ontology.Ontology;
import com.kayhut.fuse.model.ontology.OntologyUtil;
import com.kayhut.fuse.model.query.entity.ETyped;
import com.kayhut.fuse.neo4j.cypher.*;

import java.util.Map;
import java.util.Optional;

/**
 * Created by Elad on 26/03/2017.
 */
public class TypedNodeCypherStrategy extends CypherStrategy {


    public TypedNodeCypherStrategy(Map<AsgEBase, CypherCompilationState> compilationState, Ontology ont) {
        super(compilationState, ont);
    }

    public CypherCompilationState apply(AsgEBase element) {

        if (element.geteBase() instanceof ETyped) {

            ETyped eTyped = (ETyped) element.geteBase();

            CypherCompilationState curState = getRelevantState(element);

            Optional<String> label = OntologyUtil.getEntityLabel(ontology, eTyped.geteType());

            if (!label.isPresent()) {
                throw new RuntimeException("Failed compiling query. Unknown entity type: " + eTyped.geteType());
            }

            CypherNode node = CypherNode.cypherNode()
                    .withTag(eTyped.geteTag())
                    .withLabel(label.get());

            CypherReturnElement returnElement = CypherReturnElement.cypherReturnElement().withTag(node.tag);

            //create updated state with new statement
            return context(element, new CypherCompilationState(curState.getStatement().
                                                                    appendNode(curState.getPathTag(), node).
                                                                    addReturn(returnElement),
                                                                    curState.getPathTag()));
        }
        return getRelevantState(element);

    }
}

