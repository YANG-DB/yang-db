package com.yangdb.fuse.asg.translator.sparql.strategies;

import com.yangdb.fuse.asg.translator.sparql.strategies.expressions.ExpressionStrategies;
import com.yangdb.fuse.model.asgQuery.AsgQuery;
import org.eclipse.rdf4j.query.algebra.Filter;
import org.eclipse.rdf4j.query.algebra.TupleExpr;
import org.eclipse.rdf4j.query.algebra.ValueExpr;

import java.util.List;

/**
 * Filter element containing condition and args
 */
public class FilterPatternTranslatorStrategy extends UnaryPatternTranslatorStrategy{

    private final List<ExpressionStrategies> whereExpressionStrategies;

    public FilterPatternTranslatorStrategy(List<SparqlElementTranslatorStrategy> translatorStrategies, List<ExpressionStrategies> whereExpressionStrategies) {
        super(translatorStrategies);
        this.whereExpressionStrategies = whereExpressionStrategies;
    }

    @Override
    public void apply(TupleExpr element, AsgQuery query, SparqlStrategyContext context) {
        if(Filter.class.isAssignableFrom(element.getClass())) {
            //collect condition
            ValueExpr condition = ((Filter) element).getCondition();
            //apply where strategies
            whereExpressionStrategies.stream().forEach(st->st.apply(condition,query,context));
        }
        super.apply(element, query, context);
    }
}
