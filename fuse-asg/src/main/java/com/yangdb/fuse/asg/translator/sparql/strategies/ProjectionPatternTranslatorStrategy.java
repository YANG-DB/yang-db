package com.yangdb.fuse.asg.translator.sparql.strategies;

import com.yangdb.fuse.model.asgQuery.AsgQuery;
import org.eclipse.rdf4j.query.algebra.Projection;
import org.eclipse.rdf4j.query.algebra.ProjectionElemList;
import org.eclipse.rdf4j.query.algebra.TupleExpr;

import java.util.List;

/**
 * selection projection query element
 */
public class ProjectionPatternTranslatorStrategy extends UnaryPatternTranslatorStrategy{

    public ProjectionPatternTranslatorStrategy(List<SparqlElementTranslatorStrategy> translatorStrategies) {
        super(translatorStrategies);
    }

    @Override
    public void apply(TupleExpr element, AsgQuery query, SparqlStrategyContext context) {
        if(Projection.class.isAssignableFrom(element.getClass())) {
            //collect projection names
            ProjectionElemList list = ((Projection) element).getProjectionElemList();
            query.getProjectedFields().addAll(list.getTargetNames());
        }
        super.apply(element, query, context);
    }
}
