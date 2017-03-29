package com.kayhut.fuse.unipop.controller.search.appender;

import com.kayhut.fuse.unipop.controller.context.PromiseElementControllerContext;
import com.kayhut.fuse.unipop.controller.search.QueryBuilder;
import com.kayhut.fuse.unipop.controller.utils.TraversalQueryTranslator;
import com.kayhut.fuse.unipop.controller.utils.TraversalValuesByKeyProvider;
import com.kayhut.fuse.unipop.promise.TraversalConstraint;
import com.kayhut.fuse.unipop.structure.ElementType;

import java.util.Optional;
import java.util.Set;

/**
 * Created by User on 27/03/2017.
 */
public class ElementGlobalTypeSearchAppender extends SearchQueryAppenderBase<PromiseElementControllerContext> {
    //region SearchQueryAppenderBase Implementation
    @Override
    public boolean append(QueryBuilder queryBuilder, PromiseElementControllerContext context) {
        if (!context.getConstraint().isPresent()) {
            return false;
        }
        Optional<TraversalConstraint> constraint = context.getConstraint();
        if (constraint.isPresent()) {
            TraversalValuesByKeyProvider traversalValuesByKeyProvider = new TraversalValuesByKeyProvider();
            Set<String> labels = traversalValuesByKeyProvider.getValueByKey(context.getConstraint().get().getTraversal(), "label");

            // If there are labels in the constraint, this appender is not relevant, exit.
            if (!labels.isEmpty())
                return false;
        }
        // If there is no Constraint
        if (context.getElementType() == ElementType.vertex) {
            Iterable<String> vertexTypes = context.getSchemaProvider().getVertexTypes();
            queryBuilder.seekRoot().query().filtered().filter().bool().must().terms("_type", vertexTypes);
        }
        else if (context.getElementType() == ElementType.edge) {
            ;//To be continue...
        }

        return true;
    }
    //endregion
}
