package com.kayhut.fuse.unipop.controller.common.appender;

import com.kayhut.fuse.unipop.controller.common.context.ElementControllerContext;
import com.kayhut.fuse.unipop.controller.promise.appender.SearchQueryAppenderBase;
import com.kayhut.fuse.unipop.controller.search.QueryBuilder;
import com.kayhut.fuse.unipop.controller.search.SearchBuilder;
import com.kayhut.fuse.unipop.controller.utils.traversal.TraversalHasStepFinder;
import com.kayhut.fuse.unipop.controller.utils.traversal.TraversalValuesByKeyProvider;
import com.kayhut.fuse.unipop.schemaProviders.GraphElementSchema;
import com.kayhut.fuse.unipop.structure.ElementType;
import javaslang.collection.Stream;
import org.apache.tinkerpop.gremlin.structure.T;

import java.util.Collections;
import java.util.Optional;
import java.util.Set;

/**
 * Created by roman.margolis on 18/09/2017.
 */
public class ElementRoutingSearchAppender implements SearchAppender<ElementControllerContext> {
    //region SearchAppender Implementation
    @Override
    public boolean append(SearchBuilder searchBuilder, ElementControllerContext context) {
        if (!context.getConstraint().isPresent()) {
            return false;
        }

        Set<String> labels = Collections.emptySet();
        if (context.getConstraint().isPresent()) {
            TraversalValuesByKeyProvider traversalValuesByKeyProvider = new TraversalValuesByKeyProvider();
            labels = traversalValuesByKeyProvider.getValueByKey(context.getConstraint().get().getTraversal(), T.label.getAccessor());
        }

        if (labels.isEmpty()) {
            labels = Stream.ofAll(context.getElementType().equals(ElementType.vertex) ?
                    context.getSchemaProvider().getVertexLabels() :
                    context.getSchemaProvider().getEdgeLabels()).toJavaSet();
        }

        Set<String> routingPropertyNames =
                Stream.ofAll(labels)
                .map(label -> context.getElementType().equals(ElementType.vertex) ?
                              context.getSchemaProvider().getVertexSchema(label) :
                              context.getSchemaProvider().getEdgeSchema(label))
                .filter(Optional::isPresent)
                .map(elementSchema -> elementSchema.get().getRouting())
                .filter(Optional::isPresent)
                .map(routing -> routing.get().getRoutingProperty().getName())
                .toJavaSet();

        Set<String> routingValues =
        Stream.ofAll(routingPropertyNames)
                .map(propertyName -> propertyName.equals("_id") ? T.id.getAccessor() : propertyName)
                .flatMap(propertyName -> new TraversalValuesByKeyProvider().getValueByKey(context.getConstraint().get().getTraversal(), propertyName))
                .toJavaSet();

        searchBuilder.getRouting().addAll(routingValues);

        return routingValues.size() > 0;
    }
    //endregion
}
