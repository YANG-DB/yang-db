package com.kayhut.fuse.neo4j.cypher.strategy;

import com.kayhut.fuse.model.asgQuery.AsgEBase;
import com.kayhut.fuse.model.ontology.Ontology;
import com.kayhut.fuse.model.ontology.Property;
import com.kayhut.fuse.model.query.properties.constraint.Constraint;
import com.kayhut.fuse.model.query.properties.*;
import com.kayhut.fuse.model.query.quant.Quant1;
import com.kayhut.fuse.model.query.quant.Quant2;
import com.kayhut.fuse.model.query.quant.QuantType;
import com.kayhut.fuse.neo4j.cypher.CypherCompilationState;
import com.kayhut.fuse.neo4j.cypher.CypherCondition;
import com.kayhut.fuse.neo4j.cypher.CypherElement;
import com.kayhut.fuse.neo4j.cypher.CypherStatement;
import com.kayhut.fuse.neo4j.cypher.types.CypherTypeParsersFactory;

import java.util.Map;
import java.util.Optional;

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

        if(element.geteBase() instanceof EPropGroup ||
           element.geteBase() instanceof RelPropGroup) {

            CypherCompilationState curState = getRelevantState(element);

            ((BasePropGroup)element.geteBase()).getProps().forEach(prop -> applySingleProp(element, (BaseProp) prop, curState));

            return context(element, new CypherCompilationState(curState.getStatement(), curState.getPathTag()));
        }

        if (element.geteBase() instanceof EProp ||
            element.geteBase() instanceof RelProp) {

            CypherCompilationState curState = getRelevantState(element);

            applySingleProp(element, (BaseProp) element.geteBase(), curState);

            return context(element, new CypherCompilationState(curState.getStatement(), curState.getPathTag()));
        }

        return getRelevantState(element);

    }

    private void applySingleProp(AsgEBase element, BaseProp prop, CypherCompilationState curState) {

        Optional<Property> property = getProperty(prop, ontology);

        if (!property.isPresent()) {
            throw new RuntimeException("Unknown property.");
        }

        Constraint con = prop.getCon();

        CypherCondition cond = buildPropertyCondition(element, con, property.get(), curState.getStatement(), curState.getPathTag());

        curState.getStatement().appendCondition(cond);

    }

    private Optional<Property> getProperty(BaseProp prop, Ontology ont) {

        Ontology.Accessor $ont = new Ontology.Accessor(ont);

        String pType = prop.getpType();

        return $ont.$property(pType);

    }

    private CypherCondition buildPropertyCondition(AsgEBase asgNode,Constraint constraint, Property property, CypherStatement cypherStatement, String pathTag) {

        CypherElement lastElement = cypherStatement.getPath(pathTag).getElementFromEnd(1);

        String val = CypherTypeParsersFactory.toCypherValue(ontology, property.getType(), constraint.getExpr());

        Optional<AsgEBase> any = asgNode.getParents()
                                        .stream()
                                        .filter(p -> ((AsgEBase)p).geteBase() instanceof Quant1 ||
                                                     ((AsgEBase) p).geteBase() instanceof Quant2).findAny();

        CypherCondition.Condition condType = CypherCondition.Condition.AND;

        if(any.isPresent()) {

            if(any.get().geteBase() instanceof  Quant1) {
                if (((Quant1) any.get().geteBase()).getqType().equals(QuantType.all)) {
                    condType = CypherCondition.Condition.AND;
                } else {
                    condType = CypherCondition.Condition.OR;
                }
            }
            if(any.get().geteBase() instanceof  Quant2) {
                if (((Quant2) any.get().geteBase()).getqType().equals(QuantType.all)) {
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
