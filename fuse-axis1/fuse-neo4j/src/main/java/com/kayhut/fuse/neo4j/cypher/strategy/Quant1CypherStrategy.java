package com.kayhut.fuse.neo4j.cypher.strategy;

import com.kayhut.fuse.model.asgQuery.AsgEBase;
import com.kayhut.fuse.model.ontology.Ontology;
import com.kayhut.fuse.model.query.properties.EProp;
import com.kayhut.fuse.model.query.quant.Quant1;
import com.kayhut.fuse.neo4j.cypher.CypherCompilationState;
import com.kayhut.fuse.neo4j.cypher.CypherElement;
import com.kayhut.fuse.neo4j.cypher.CypherNode;
import com.kayhut.fuse.neo4j.cypher.CypherStatement;

import java.util.List;
import java.util.Map;

/**
 * Created by Elad on 4/2/2017.
 */
public class Quant1CypherStrategy extends CypherStrategy {

    private static final String OR_QUANT_TYPE = "some";
    private static final String AND_QUANT_TYPE = "all";

    public Quant1CypherStrategy(Map<AsgEBase, CypherCompilationState> compilationState, Ontology ont) {
        super(compilationState, ont);
    }

    @Override
    public CypherCompilationState apply(AsgEBase element) {

        if (element.geteBase() instanceof Quant1) {

            Quant1 quant1 = (Quant1) element.geteBase();

            //A quantifier has one connection on its left side, and two or more branches on its right side
            //In quantifiers of type 1, the left side always ends with an entity, and the right side starts with:
            //          quantifier, path, relation or entity's property.

            CypherCompilationState curState = getRelevantState(element);

            //get the entity on the left side of the quantifier
            CypherElement lastElement = curState.getStatement().getPath(curState.getPathTag()).getElementFromEnd(1);

            if (!(lastElement instanceof CypherNode)) {
                //Illegal use of Quant1!
                throw new RuntimeException("Failed to compile query. Illegal use of Quant1.");
            }

            CypherNode node = (CypherNode) lastElement;

            List<AsgEBase> children = element.getNext();
            boolean isNewBranchCreated = false;
            for (AsgEBase child : children) {
                if (child.geteBase() instanceof EProp) {
                    // just pass the state forward, and let the Eprop add its condition later.
                    context(child, curState);
                } else {
                    if (!isNewBranchCreated) {
                        //keep left branch
                        isNewBranchCreated = true;
                        context(child, curState);
                    } else {
                        if (quant1.getqType().equals(OR_QUANT_TYPE)) {
                            //open new branch
                            context(child, new CypherCompilationState(curState.getStatement().copy(), curState.getPathTag()));
                        } else if (quant1.getqType().equals(AND_QUANT_TYPE)) {
                            //open new path starting at the last node
                            String nextPathTag = curState.getStatement().getNextPathTag();
                            CypherStatement updatedStatement = curState.getStatement().appendNode(nextPathTag,
                                                                                    CypherNode.cypherNode().withTag(node.tag));
                            context(child, new CypherCompilationState(updatedStatement, nextPathTag));
                        }
                    }
                }
            }
        }

        return getRelevantState(element);
    }
}
