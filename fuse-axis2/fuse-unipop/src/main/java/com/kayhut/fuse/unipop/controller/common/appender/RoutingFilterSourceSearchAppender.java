package com.kayhut.fuse.unipop.controller.common.appender;

import com.kayhut.fuse.unipop.controller.common.context.ElementControllerContext;
import com.kayhut.fuse.unipop.controller.common.context.SchemaProviderContext;
import com.kayhut.fuse.unipop.controller.common.context.SelectContext;
import com.kayhut.fuse.unipop.controller.discrete.util.SchemaUtil;
import com.kayhut.fuse.unipop.controller.search.SearchBuilder;
import com.kayhut.fuse.unipop.controller.utils.traversal.TraversalValuesByKeyProvider;
import com.kayhut.fuse.unipop.predicates.SelectP;
import com.kayhut.fuse.unipop.structure.ElementType;
import javaslang.collection.Stream;
import org.apache.tinkerpop.gremlin.process.traversal.P;
import org.apache.tinkerpop.gremlin.process.traversal.step.util.HasContainer;
import org.apache.tinkerpop.gremlin.structure.T;

import java.util.*;

/**
 * Created by roman.margolis on 18/09/2017.
 */
public class RoutingFilterSourceSearchAppender implements SearchAppender<ElementControllerContext> {
    //region SearchAppender Implementation
    @Override
    public boolean append(SearchBuilder searchBuilder, ElementControllerContext context) {
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

        Set<String> finalLabels = labels;

        Set<String> routingFields = Stream.ofAll(labels)
                .map(label -> context.getElementType().equals(ElementType.vertex) ?
                        context.getSchemaProvider().getVertexSchema(label) :
                        context.getSchemaProvider().getEdgeSchema(label))
                .filter(Optional::isPresent)
                .map(schema -> schema.get().getRouting())
                .filter(Optional::isPresent)
                .map(routing -> routing.get().getRoutingProperty().getName())
                .toJavaSet();

        // gather additional routing fields of vertices for edges
        if (context.getElementType().equals(ElementType.vertex)) {
            routingFields.addAll(
                    Stream.ofAll(context.getSchemaProvider().getEdgeLabels())
                    .map(label -> context.getSchemaProvider().getEdgeSchema(label))
                    .flatMap(edgeSchema -> Arrays.asList(edgeSchema.get().getSource(), edgeSchema.get().getDestination()))
                    .filter(Optional::isPresent)
                    .filter(endSchema -> finalLabels.contains(endSchema.get().getLabel().get()))
                    .filter(endSchema -> endSchema.get().getRouting().isPresent())
                    .map(endSchema -> endSchema.get().getRouting().get().getRoutingProperty().getName())
                    .toJavaSet());
        }

        searchBuilder.getIncludeSourceFields().addAll(routingFields);

        return routingFields.size() > 0;
    }
    //endregion
}
