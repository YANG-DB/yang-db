package com.kayhut.fuse.neo4j.cypher.strategy;

import com.kayhut.fuse.model.asgQuery.AsgEBase;
import com.kayhut.fuse.model.ontology.Ontology;
import com.kayhut.fuse.model.query.entity.EUntyped;
import com.kayhut.fuse.neo4j.cypher.CypherCompilationState;
import com.kayhut.fuse.neo4j.cypher.CypherNode;
import com.kayhut.fuse.neo4j.cypher.CypherReturnElement;

import java.util.Map;

/**
 * Created by Elad on 6/8/2017.
 */
public class UnTypedNodeCypherStrategy extends CypherStrategy {

    private Ontology.Accessor $ont;

    public UnTypedNodeCypherStrategy(Map<AsgEBase, CypherCompilationState> compilationState, Ontology ont) {
        super(compilationState, ont);
        $ont = new Ontology.Accessor(ont);
    }

    @Override
    public CypherCompilationState apply(AsgEBase element) {
        if (element.geteBase() instanceof EUntyped) {

            EUntyped eUntyped = (EUntyped) element.geteBase();

            //TODO: Support valid \ Not-valid types

            CypherCompilationState curState = getRelevantState(element);

            String tag = eUntyped.geteTag() == null ? curState.getStatement().getNewNodeTag() : eUntyped.geteTag();

            CypherNode node = CypherNode.cypherNode().withTag(tag);

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
