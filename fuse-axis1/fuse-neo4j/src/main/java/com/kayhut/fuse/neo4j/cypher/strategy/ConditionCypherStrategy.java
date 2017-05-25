package com.kayhut.fuse.neo4j.cypher.strategy;

import com.kayhut.fuse.model.asgQuery.AsgEBase;
import com.kayhut.fuse.model.ontology.Ontology;
import com.kayhut.fuse.model.ontology.Property;
import com.kayhut.fuse.model.query.Constraint;
import com.kayhut.fuse.model.query.EBase;
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

        if (element.geteBase() instanceof EProp || element.geteBase() instanceof RelProp) {

            CypherCompilationState curState = getRelevantState(element);

            Optional<Property> property = getProperty(element, ontology);

            if(!property.isPresent()) {
                throw new RuntimeException("Unknown property.");
            }

            CypherCondition cond = buildPropertyCondition(element, property.get(), curState.getStatement(), curState.getPathTag(), ontology);

            curState.getStatement().appendCondition(cond);

            return context(element, new CypherCompilationState(curState.getStatement(), curState.getPathTag()));

        }

        return getRelevantState(element);

    }

    private String getPropertyType(EBase eBase) {
        if(eBase instanceof EProp) {
            return ((EProp)eBase).getpType();
        } else {
            return ((RelProp)eBase).getpType();
        }
    }

    private Constraint getConstraint(EBase eBase) {
        if(eBase instanceof EProp) {
            return ((EProp)eBase).getCon();
        } else {
            return ((RelProp)eBase).getCon();
        }
    }

    private Optional<Property> getProperty(AsgEBase asgNode, Ontology ont) {
        Ontology.Accessor $ont = new Ontology.Accessor(ont);

        String pType = getPropertyType(asgNode.geteBase());

        //Need to traverse the tree bottom-up, and find the first parent entity element.

        Queue<AsgEBase> parents = new LinkedList<>(asgNode.getParents());

        while (!parents.isEmpty()) {

            AsgEBase p = parents.poll();

            if (asgNode.geteBase() instanceof EProp && p.geteBase() instanceof ETyped) {
                return $ont.$property(Integer.parseInt(pType));
            } else if (asgNode.geteBase() instanceof RelProp && p.geteBase() instanceof Rel) {
                return $ont.$property(Integer.parseInt(pType));
            } else {
                parents.addAll(p.getParents());
            }
        }

        return null;
    }

    private CypherCondition buildPropertyCondition(AsgEBase asgNode,Property property, CypherStatement cypherStatement, String pathTag, Ontology ont) {

        CypherElement lastElement = cypherStatement.getPath(pathTag).getElementFromEnd(1);

        Constraint constraint = getConstraint(asgNode.geteBase());

        String val = property.getType().equals("int") || constraint.getExpr() == null ?
                                                            (String)constraint.getExpr() :
                                                            "'" + constraint.getExpr() + "'";

        Optional any = asgNode.getParents()
                              .stream()
                              .filter(p -> ((AsgEBase)p).geteBase() instanceof Quant1 ||
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
                .withTarget(String.format("%s.%s", lastElement.tag, property.getName().replace(" ","_")))
                .withValue(val)
                .withOperator(getOp(constraint.getOp()))
                .withType(condType);

        return cond;

    }

}
