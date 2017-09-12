package com.kayhut.fuse.unipop.controller.promise.appender;

import com.kayhut.fuse.unipop.controller.common.context.ConstraintContext;
import com.kayhut.fuse.unipop.controller.search.QueryBuilder;
import com.kayhut.fuse.unipop.controller.search.SearchBuilder;
import com.kayhut.fuse.unipop.controller.utils.traversal.TraversalQueryTranslator;
import com.kayhut.fuse.unipop.promise.TraversalConstraint;

import java.util.Optional;

/**
 * Created by Elad on 4/26/2017.
 */
public class EdgeConstraintSearchAppender implements SearchAppender<ConstraintContext> {
    @Override
    public boolean append(SearchBuilder searchBuilder, ConstraintContext context) {
        Optional<TraversalConstraint> constraint = context.getConstraint();
        if(!constraint.isPresent()) {
            return true;
        }
        QueryBuilder queryBuilder = searchBuilder.getQueryBuilder().seekRoot().query().filtered().filter().bool().must();
        TraversalQueryTranslator traversalQueryTranslator = new TraversalQueryTranslator(queryBuilder, false);
        traversalQueryTranslator.visit(constraint.get().getTraversal());
        return true;
    }
}
