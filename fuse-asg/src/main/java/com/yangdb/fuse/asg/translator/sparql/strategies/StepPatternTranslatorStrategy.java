package com.yangdb.fuse.asg.translator.sparql.strategies;

import com.yangdb.fuse.model.asgQuery.AsgQuery;
import org.eclipse.rdf4j.query.algebra.StatementPattern;
import org.eclipse.rdf4j.query.algebra.TupleExpr;
import org.eclipse.rdf4j.query.algebra.Var;

public class StepPatternTranslatorStrategy implements SparqlElementTranslatorStrategy{

    @Override
    public void apply(TupleExpr element, AsgQuery query, SparqlStrategyContext context) {
        //todo verify edge creation (type & relation)
        if(StatementPattern.class.isAssignableFrom(element.getClass())) {
            Var objectVar = ((StatementPattern) element).getObjectVar();
            Var predicateVar = ((StatementPattern) element).getPredicateVar();
            Var subjectVar = ((StatementPattern) element).getSubjectVar();
            Var contextVar = ((StatementPattern) element).getContextVar();
        }
    }
}
