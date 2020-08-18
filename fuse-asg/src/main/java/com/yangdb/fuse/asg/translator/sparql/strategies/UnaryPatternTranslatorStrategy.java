package com.yangdb.fuse.asg.translator.sparql.strategies;

import com.yangdb.fuse.model.asgQuery.AsgQuery;
import org.eclipse.rdf4j.query.algebra.TupleExpr;
import org.eclipse.rdf4j.query.algebra.UnaryTupleOperator;

import java.util.List;

/**
 * base class for all unary operators
 */
public abstract class UnaryPatternTranslatorStrategy implements SparqlElementTranslatorStrategy{
    private List<SparqlElementTranslatorStrategy> translatorStrategies;

    public UnaryPatternTranslatorStrategy(List<SparqlElementTranslatorStrategy> translatorStrategies) {
        this.translatorStrategies = translatorStrategies;
    }

    @Override
    public void apply(TupleExpr element, AsgQuery query, SparqlStrategyContext context) {
        if(UnaryTupleOperator.class.isAssignableFrom(element.getClass())) {
            //collect projection names
            TupleExpr arg = ((UnaryTupleOperator) element).getArg();
            translatorStrategies.forEach(st->st.apply(arg,query,context));
        }
    }
}
