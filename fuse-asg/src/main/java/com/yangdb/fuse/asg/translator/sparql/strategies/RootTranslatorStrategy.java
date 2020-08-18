package com.yangdb.fuse.asg.translator.sparql.strategies;

import com.yangdb.fuse.asg.translator.sparql.strategies.expressions.ExpressionStrategies;
import com.yangdb.fuse.model.asgQuery.AsgQuery;
import org.eclipse.rdf4j.query.Dataset;
import org.eclipse.rdf4j.query.algebra.TupleExpr;
import org.eclipse.rdf4j.query.parser.ParsedQuery;

import java.util.List;


/**
 * Main entry point for the AST transformations
 */
public class RootTranslatorStrategy implements SparqlTranslatorStrategy{
    private final List<SparqlElementTranslatorStrategy> translatorStrategies;
    private final List<ExpressionStrategies> whereExpressionStrategies;

    public RootTranslatorStrategy(List<SparqlElementTranslatorStrategy> translatorStrategies, List<ExpressionStrategies> whereExpressionStrategies) {
        this.translatorStrategies = translatorStrategies;
        this.whereExpressionStrategies = whereExpressionStrategies;
    }

    @Override
    public void apply(AsgQuery query, SparqlStrategyContext context) {
        ParsedQuery statement = context.getStatement();
        Dataset dataset = statement.getDataset();
        TupleExpr expr = statement.getTupleExpr();
        translatorStrategies.iterator().next().apply(expr,query,context);
    }


}
