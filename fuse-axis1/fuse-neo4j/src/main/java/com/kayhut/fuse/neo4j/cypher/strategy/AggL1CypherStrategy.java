package com.kayhut.fuse.neo4j.cypher.strategy;

import com.kayhut.fuse.model.asgQuery.AsgEBase;
import com.kayhut.fuse.model.ontology.Ontology;
import com.kayhut.fuse.model.query.properties.constraint.Constraint;
import com.kayhut.fuse.model.query.aggregation.AggL1;
import com.kayhut.fuse.neo4j.cypher.*;

import java.util.Map;

/**
 * Created by Elad on 4/2/2017.
 */
public class AggL1CypherStrategy extends CypherStrategy {

    public AggL1CypherStrategy(Map<AsgEBase, CypherCompilationState> compilationState, Ontology ont) {
        super(compilationState, ont);
    }

    @Override
    public CypherCompilationState apply(AsgEBase element) {

        if(element.geteBase() instanceof AggL1) {

            AggL1 agg = (AggL1) element.geteBase();

            String[] per = agg.getPer();
            String eTag = agg.getETag();
            Constraint con = agg.getCon();

            CypherCompilationState curState = getRelevantState(element);

            //Add aggregation pattern by using a with clause
            // WITH [per.1, per.2, per.3, ...] , aggregation( eTag ) as AGG
            // WHERE constraint(AGG)

            for (String perTag :
                    per) {

                //TODO: get from statement ?
                String aggAlias = curState.getStatement().getNewAggTag();

                CypherReturnElement aggElement = CypherReturnElement.cypherReturnElement()
                                                                    .withTag(perTag)
                                                                    .withFunction("count")
                                                                    .withAlias(aggAlias);
                //add aggregation in with clause
                curState.getStatement().addWith(aggElement);

                //add aggregated result to return clause
                curState.getStatement().addReturn(CypherReturnElement.cypherReturnElement().withTag(aggAlias));

                //add condition on the aggregation
                CypherCondition condition = CypherCondition.cypherCondition()
                                                           .withTarget(aggAlias)
                                                           .withOperator(CypherOps.getOp(con.getOp()))
                                                           .withValue(con.getExpr() instanceof String ? "'" + con.getExpr() + "'" : String.valueOf(con.getExpr()))
                                                           .withType(CypherCondition.Condition.AND);

                curState.getStatement().appendCondition(condition);
            }

            return context(element, curState);

        }

        return getRelevantState(element);
    }
}
