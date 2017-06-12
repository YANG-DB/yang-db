package com.kayhut.fuse.neo4j.cypher.strategy;

import com.kayhut.fuse.model.asgQuery.AsgEBase;
import com.kayhut.fuse.model.ontology.Ontology;
import com.kayhut.fuse.model.query.properties.EProp;
import com.kayhut.fuse.model.query.quant.Quant2;
import com.kayhut.fuse.model.query.quant.QuantType;
import com.kayhut.fuse.neo4j.cypher.CypherCompilationState;
import com.kayhut.fuse.neo4j.cypher.CypherElement;
import com.kayhut.fuse.neo4j.cypher.CypherRelationship;

import java.util.List;
import java.util.Map;

/**
 * Created by Elad on 4/2/2017.
 */
public class Quant2CypherStrategy extends CypherStrategy {
    public Quant2CypherStrategy(Map<AsgEBase, CypherCompilationState> compilationState, Ontology ont) {
        super(compilationState, ont);
    }

    @Override
    public CypherCompilationState apply(AsgEBase element) {

        if (element.geteBase() instanceof Quant2) {

            Quant2 quant2 = (Quant2) element.geteBase();

            //A quantifier has one connection on its L side, and two or more branches on its R side
            //In quantifiers of type 2, the L side always ends with a relation or a path, and the R side starts with:
            //          quantifier or an entity.

            CypherCompilationState curState = getRelevantState(element);

            //get the entity on the L side of the quantifier
            CypherElement lastElement = curState.getStatement().getPath(curState.getPathTag()).getElementFromEnd(1);

            if (!(lastElement instanceof CypherRelationship)) {
                //Illegal use of Quant2!
                throw new RuntimeException("Failed to compile query. Illegal use of Quant2.");
            }

            CypherRelationship rel = (CypherRelationship) lastElement;

            List<AsgEBase> children = element.getNext();
            boolean isNewBranchCreated = false;
            for (AsgEBase child : children) {
                if (child.geteBase() instanceof EProp) {
                    // just pass the state forward, and let the Eprop add its condition later.
                    context(child, curState);
                } else {
                    if (!isNewBranchCreated) {
                        //keep L branch
                        isNewBranchCreated = true;
                        context(child, curState);
                    } else {
                        if (quant2.getqType().equals(QuantType.some)) {
                            //open new branch
                            context(child, new CypherCompilationState(curState.getStatement().copy(), curState.getPathTag()));
                        } else if (quant2.getqType().equals(QuantType.all)) {
                            //open new path
                            context(child, new CypherCompilationState(curState.getStatement(), curState.getStatement().getNextPathTag()));
                        }
                    }
                }
            }

        }

        return getRelevantState(element);
    }
}
