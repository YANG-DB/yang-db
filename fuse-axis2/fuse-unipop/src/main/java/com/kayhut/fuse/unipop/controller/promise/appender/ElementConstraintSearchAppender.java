package com.kayhut.fuse.unipop.controller.promise.appender;

import com.kayhut.fuse.unipop.controller.common.context.CompositeControllerContext;
import com.kayhut.fuse.unipop.controller.common.context.ElementControllerContext;
import com.kayhut.fuse.unipop.controller.promise.context.PromiseElementControllerContext;
import com.kayhut.fuse.unipop.controller.search.QueryBuilder;
import com.kayhut.fuse.unipop.controller.utils.traversal.TraversalQueryTranslator;

/**
 * Created by User on 27/03/2017.
 */
public class ElementConstraintSearchAppender extends SearchQueryAppenderBase<ElementControllerContext> {
    //region SearchQueryAppenderBase Implementation
    @Override
    protected boolean append(QueryBuilder queryBuilder, ElementControllerContext context) {
        if (!context.getConstraint().isPresent()) {
            return false;
        }
        new TraversalQueryTranslator(queryBuilder.seekRoot().query().bool().filter().bool().must(), false)
                .visit(context.getConstraint().get().getTraversal());

        return true;
    }
    //endregion
}
