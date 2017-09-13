package com.kayhut.fuse.unipop.controller.common.appender;

import com.kayhut.fuse.unipop.controller.common.context.ConstraintContext;
import com.kayhut.fuse.unipop.controller.common.context.ElementControllerContext;
import com.kayhut.fuse.unipop.controller.promise.appender.SearchQueryAppenderBase;
import com.kayhut.fuse.unipop.controller.promise.context.PromiseElementControllerContext;
import com.kayhut.fuse.unipop.controller.search.QueryBuilder;
import com.kayhut.fuse.unipop.controller.utils.traversal.TraversalValuesByKeyProvider;
import com.kayhut.fuse.unipop.promise.TraversalConstraint;
import com.kayhut.fuse.unipop.structure.ElementType;
import org.apache.tinkerpop.gremlin.structure.T;

import java.util.Optional;
import java.util.Set;

/**
 * Created by User on 27/03/2017.
 */
public class ElementGlobalTypeSearchAppender extends SearchQueryAppenderBase<ElementControllerContext> {
    //region SearchQueryAppenderBase Implementation
    @Override
    public boolean append(QueryBuilder queryBuilder, ElementControllerContext context) {
        if (!context.getConstraint().isPresent()) {
            return false;
        }
        Optional<TraversalConstraint> constraint = context.getConstraint();
        if (constraint.isPresent()) {
            TraversalValuesByKeyProvider traversalValuesByKeyProvider = new TraversalValuesByKeyProvider();
            Set<String> labels = traversalValuesByKeyProvider.getValueByKey(context.getConstraint().get().getTraversal(), T.label.getAccessor());

            // If there are labels in the constraint, this appender is not relevant, exit.
            if (!labels.isEmpty())
                return false;
        }
        // If there is no Constraint
        if (context.getElementType() == ElementType.vertex) {
            Iterable<String> vertexTypes = context.getSchemaProvider().getVertexTypes();
            queryBuilder.seekRoot().query().filtered().filter().bool().must().terms(this.getClass().getSimpleName(),"_type", vertexTypes);
        }
        else if (context.getElementType() == ElementType.edge) {
            ;//To be continue...
        }

        return true;
    }
    //endregion
}
