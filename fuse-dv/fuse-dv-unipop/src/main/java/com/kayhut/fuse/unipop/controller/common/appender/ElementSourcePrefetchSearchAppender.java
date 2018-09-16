package com.kayhut.fuse.unipop.controller.common.appender;

import com.kayhut.fuse.unipop.controller.common.context.CompositeControllerContext;
import com.kayhut.fuse.unipop.controller.common.context.ElementControllerContext;
import com.kayhut.fuse.unipop.controller.common.context.VertexControllerContext;
import com.kayhut.fuse.unipop.controller.search.SearchBuilder;
import com.kayhut.fuse.unipop.controller.utils.traversal.TraversalValuesByKeyProvider;
import com.kayhut.fuse.unipop.schemaProviders.GraphEdgeSchema;
import com.kayhut.fuse.unipop.structure.ElementType;
import javaslang.collection.Stream;
import org.apache.tinkerpop.gremlin.structure.Element;
import org.apache.tinkerpop.gremlin.structure.T;

import java.util.Collections;
import java.util.Set;

public class ElementSourcePrefetchSearchAppender implements SearchAppender<CompositeControllerContext> {
    //region SearchAppender Implementation
    @Override
    public boolean append(SearchBuilder searchBuilder, CompositeControllerContext context) {
        if (!context.getElementType().equals(ElementType.vertex)) {
            return false;
        }

        Set<String> labels = getContextRelevantLabels(context);

        Set<String> prefetchedIdFields =
                Stream.ofAll(context.getSchemaProvider().getEdgeLabels())
                .flatMap(label -> context.getSchemaProvider().getEdgeSchemas(label))
                .filter(edgeSchema -> labels.contains(edgeSchema.getEndA().get().getLabel().get()) ||
                    labels.contains(edgeSchema.getEndB().get().getLabel().get()))
                .map(edgeSchema -> labels.contains(edgeSchema.getEndA().get().getLabel().get()) ?
                    edgeSchema.getEndB().get() : edgeSchema.getEndA().get())
                .flatMap(GraphEdgeSchema.End::getIdFields)
                .toJavaSet();

        searchBuilder.getIncludeSourceFields().addAll(prefetchedIdFields);
        return true;
    }
    //endregion

    //region Private Methods
    private Set<String> getContextRelevantLabels(CompositeControllerContext context) {
        if (context.getVertexControllerContext().isPresent()) {
            return getVertexContextRelevantLabels(context);
        }

        return getElementContextRelevantLabels(context);
    }

    private Set<String> getElementContextRelevantLabels(ElementControllerContext context) {
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

        return labels;
    }

    private Set<String> getVertexContextRelevantLabels(VertexControllerContext context) {
        // currently assuming homogeneous bulk
        return Stream.ofAll(context.getBulkVertices())
                .take(1)
                .map(Element::label)
                .toJavaSet();
    }
    //endregion
}
