package com.kayhut.fuse.unipop.controller.common.appender;

import com.kayhut.fuse.unipop.controller.common.context.ElementControllerContext;
import com.kayhut.fuse.unipop.controller.promise.appender.SearchQueryAppenderBase;
import com.kayhut.fuse.unipop.controller.search.QueryBuilder;
import com.kayhut.fuse.unipop.controller.utils.traversal.TraversalValuesByKeyProvider;
import com.kayhut.fuse.unipop.promise.TraversalConstraint;
import com.kayhut.fuse.unipop.structure.ElementType;
import javaslang.collection.Stream;
import org.apache.tinkerpop.gremlin.structure.T;

import java.util.Optional;
import java.util.Set;

/**
 * Created by User on 27/03/2017.
 */
@Deprecated
public class ElementGlobalTypeSearchAppender extends SearchQueryAppenderBase<ElementControllerContext> {
    //region SearchQueryAppenderBase Implementation
    @Override
    public boolean append(QueryBuilder queryBuilder, ElementControllerContext context) {
       /* OptionalComp<TraversalConstraint> constraint = context.getConstraint();
        if (constraint.isPresent()) {
            TraversalValuesByKeyProvider traversalValuesByKeyProvider = new TraversalValuesByKeyProvider();
            Set<String> labels = traversalValuesByKeyProvider.getValueByKey(context.getConstraint().get().getTraversal(), T.label.getAccessor());

            // If there are labels in the constraint, this appender is not relevant, exit.
            if (!labels.isEmpty())
                return false;
        }
        // If there is no Constraint
        if (context.getElementType() == ElementType.vertex) {
            Iterable<String> vertexLabels = Stream.ofAll(context.getSchemaProvider().getVertexLabels())
                    .map(label -> context.getSchemaProvider().getVertexSchemas(label).get().getType())
                    .toJavaList();
            queryBuilder.seekRoot().query().filtered().filter().bool().must().terms(this.getClass().getSimpleName(),"_type", vertexLabels);
        }
        else if (context.getElementType() == ElementType.edge) {
            Iterable<String> edgeLabels = Stream.ofAll(context.getSchemaProvider().getEdgeLabels())
                    .map(label -> context.getSchemaProvider().getEdgeSchema(label).get().getType())
                    .toJavaList();
            queryBuilder.seekRoot().query().filtered().filter().bool().must().terms(this.getClass().getSimpleName(),"_type", edgeLabels);
        }*/

        return true;
    }
    //endregion
}
