package com.kayhut.fuse.unipop.controller.search.appender;

import com.kayhut.fuse.unipop.controller.context.PromiseElementControllerContext;
import com.kayhut.fuse.unipop.controller.search.QueryBuilder;
import com.kayhut.fuse.unipop.controller.utils.TraversalQueryTranslator;

/**
 * Created by User on 27/03/2017.
 */
public class ElementConstraintSearchAppender extends SearchQueryAppenderBase<PromiseElementControllerContext> {
    //region SearchQueryAppenderBase Implementation
    @Override
    protected boolean append(QueryBuilder queryBuilder, PromiseElementControllerContext promiseElementControllerContext) {
        if (!promiseElementControllerContext.getConstraint().isPresent()) {
            return false;
        }

        new TraversalQueryTranslator(queryBuilder.seekRoot().query().filtered().filter().bool().must(), false)
                .visit(promiseElementControllerContext.getConstraint().get().getTraversal());

        return true;
    }
    //endregion
}
