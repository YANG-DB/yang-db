package com.kayhut.fuse.neo4j.cypher.strategy;

import com.kayhut.fuse.model.asgQuery.AsgEBase;
import com.kayhut.fuse.model.ontology.Ontology;
import com.kayhut.fuse.model.ontology.OntologyUtil;
import com.kayhut.fuse.model.ontology.Property;
import com.kayhut.fuse.model.query.Constraint;
import com.kayhut.fuse.model.query.ConstraintOp;
import com.kayhut.fuse.model.query.Rel;
import com.kayhut.fuse.model.query.entity.ETyped;
import com.kayhut.fuse.model.query.properties.EProp;
import com.kayhut.fuse.model.query.properties.RelProp;
import com.kayhut.fuse.model.query.quant.Quant1;
import com.kayhut.fuse.model.query.quant.Quant2;
import com.kayhut.fuse.neo4j.cypher.CypherCompilationState;
import com.kayhut.fuse.neo4j.cypher.CypherCondition;
import com.kayhut.fuse.neo4j.cypher.CypherElement;
import com.kayhut.fuse.neo4j.cypher.CypherStatement;

import java.util.LinkedList;
import java.util.Map;
import java.util.Optional;
import java.util.Queue;

import static com.kayhut.fuse.neo4j.cypher.CypherOps.getOp;

/**
 * Created by User on 26/03/2017.
 */
public class ConditionCypherStrategy extends CypherStrategy {


    public ConditionCypherStrategy(Map<AsgEBase, CypherCompilationState> compilationState, Ontology ont) {
        super(compilationState, ont);
    }

    @Override
    public CypherCompilationState apply(AsgEBase element) {
        if (element.geteBase() instanceof EProp) {

            CypherCompilationState curState = getRelevantState(element);

            CypherCondition cond = getPropertyCondition(element, curState.getStatement(), curState.getPathTag(), ontology);

            curState.getStatement().appendCondition(cond);

            return context(element, new CypherCompilationState(curState.getStatement(), curState.getPathTag()));

        }
        return getRelevantState(element);
    }

    private Optional<Property> getProperty(AsgEBase asgNode, Ontology ont) {

        Queue<AsgEBase> parents = new LinkedList<>(asgNode.getParents());

        int pType = 0;
        if (asgNode.geteBase() instanceof EProp) {
            pType = Integer.valueOf(((EProp) asgNode.geteBase()).getpType());
        } else if (asgNode.geteBase() instanceof RelProp) {
            pType = Integer.valueOf(((RelProp) asgNode.geteBase()).getpType());
        }

        while (!parents.isEmpty()) {
            AsgEBase p = parents.poll();
            if (p.geteBase() instanceof ETyped) {
                return OntologyUtil.getProperty(ont,((ETyped) p.geteBase()).geteType(), pType);
            }
            if (p.geteBase() instanceof Rel) {
                return OntologyUtil.getProperty(ont,((Rel) p.geteBase()).getrType(), pType);
            } else {
                parents.addAll(p.getParents());
            }
        }
        return null;
    }

    private CypherCondition getPropertyCondition(AsgEBase asgNode, CypherStatement cypherStatement, String pathTag, Ontology ont) {

        CypherElement lastElement = cypherStatement.getPath(pathTag).getElementFromEnd(1);

        Optional<Property> property = getProperty(asgNode, ont);

        if(!property.isPresent()) {
            // ??
            throw new RuntimeException("Could not find property for Eprop.");
        }

        Constraint con = asgNode.geteBase() instanceof EProp ? ((EProp)asgNode.geteBase()).getCon() : ((RelProp)asgNode.geteBase()).getCon();

        ConstraintOp op = con.getOp();

        String val = property.get().getType().equals("int") ? (String) con.getExpr() : "'" + con.getExpr() + "'";

        Optional any = asgNode.getParents().stream().filter(p -> ((AsgEBase) p).geteBase() instanceof Quant1 ||
                                                            ((AsgEBase) p).geteBase() instanceof Quant2).findAny();

        CypherCondition.Condition condType = CypherCondition.Condition.AND;
        if(any.isPresent()) {
            if(any.get() instanceof  Quant1) {
                if (((Quant1) any.get()).getqType().equals("all")) {
                    condType = CypherCondition.Condition.AND;
                } else {
                    condType = CypherCondition.Condition.OR;
                }
            }
            if(any.get() instanceof  Quant2) {
                if (((Quant2) any.get()).getqType().equals("all")) {
                    condType = CypherCondition.Condition.AND;
                } else {
                    condType = CypherCondition.Condition.OR;
                }
            }
        }

        CypherCondition cond = CypherCondition.cypherCondition()
                .withTarget(String.format("%s.%s", lastElement.tag, property.get().getName().replace(" ","_")))
                .withValue(val)
                .withOperator(getOp(op))
                .withType(condType);

        return cond;

    }

}
