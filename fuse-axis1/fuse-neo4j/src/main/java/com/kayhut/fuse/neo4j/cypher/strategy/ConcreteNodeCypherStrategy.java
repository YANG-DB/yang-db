package com.kayhut.fuse.neo4j.cypher.strategy;

import com.kayhut.fuse.model.asgQuery.AsgEBase;
import com.kayhut.fuse.model.ontology.Ontology;
import com.kayhut.fuse.model.query.entity.EConcrete;
import com.kayhut.fuse.neo4j.cypher.CypherCompilationState;
import com.kayhut.fuse.neo4j.cypher.CypherCondition;
import com.kayhut.fuse.neo4j.cypher.CypherNode;
import com.kayhut.fuse.neo4j.cypher.CypherReturnElement;

import java.util.Map;

/**
 * Created by Elad on 4/2/2017.
 */
public class ConcreteNodeCypherStrategy extends CypherStrategy {

    public ConcreteNodeCypherStrategy(Map<AsgEBase, CypherCompilationState> compilationState, Ontology ont) {
        super(compilationState, ont);
    }

    @Override
    public CypherCompilationState apply(AsgEBase element) {

        if (element.geteBase() instanceof EConcrete) {

            EConcrete eConcrete = (EConcrete) element.geteBase();

            CypherCompilationState curState = getRelevantState(element);

            CypherNode node = CypherNode.cypherNode()
                    .withTag(eConcrete.geteTag());

            CypherReturnElement returnElement = CypherReturnElement.cypherReturnElement(node);

            CypherCondition cond = CypherCondition.cypherCondition()
                    .withTarget(eConcrete.geteTag())
                    .withTargetFunc("id")
                    .withValue(eConcrete.geteID())
                    .withOperator("=")
                    .withType(CypherCondition.Condition.AND);

            //create updated state with new statement
            return context(element, new CypherCompilationState(curState.getStatement()
                    .appendNode(curState.getPathTag(), node)
                    .appendCondition(cond)
                    .addReturn(returnElement),
                    curState.getPathTag()));
        }
        return getRelevantState(element);
    }
}
