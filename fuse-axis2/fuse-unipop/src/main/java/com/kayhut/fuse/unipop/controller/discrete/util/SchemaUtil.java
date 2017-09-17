package com.kayhut.fuse.unipop.controller.discrete.util;

import com.kayhut.fuse.unipop.controller.common.context.VertexControllerContext;
import com.kayhut.fuse.unipop.controller.utils.traversal.TraversalValuesByKeyProvider;
import com.kayhut.fuse.unipop.schemaProviders.GraphEdgeSchema;
import javaslang.collection.Stream;
import org.apache.tinkerpop.gremlin.structure.Direction;
import org.apache.tinkerpop.gremlin.structure.T;

import java.util.Collections;
import java.util.Set;

/**
 * Created by roman.margolis on 14/09/2017.
 */
public class SchemaUtil {
    public static Iterable<GraphEdgeSchema> getEdgeSchemas(VertexControllerContext context) {
        Set<String> types = Collections.emptySet();
        if (context.getConstraint().isPresent()) {
            TraversalValuesByKeyProvider traversalValuesByKeyProvider = new TraversalValuesByKeyProvider();
            types = traversalValuesByKeyProvider.getValueByKey(context.getConstraint().get().getTraversal(), T.label.getAccessor());
        }

        if (types.isEmpty()) {
            types = Stream.ofAll(context.getSchemaProvider().getEdgeLabels()).toJavaSet();
        }

        return Stream.ofAll(types)
                .flatMap(type -> context.getSchemaProvider().getEdgeSchemas(type))
                .distinct()
                .toJavaList();
    }

    public static Iterable<GraphEdgeSchema> getSingularEdgeSchemas(VertexControllerContext context) {
        return Stream.ofAll(getEdgeSchemas(context))
                .filter(edgeSchema -> !edgeSchema.getDirection().isPresent())
                .toJavaList();
    }

    public static Iterable<GraphEdgeSchema> getRelevantSingularEdgeSchemas(VertexControllerContext context) {
        //currently assuming all bulk vertices of same type
        String vertexLabel = Stream.ofAll(context.getBulkVertices()).get(0).label();

        return Stream.ofAll(getSingularEdgeSchemas(context))
                .filter(edgeSchema -> (edgeSchema.getSource().get().getLabel().get().equals(vertexLabel) &&
                        (context.getDirection().equals(Direction.OUT) || context.getDirection().equals(Direction.BOTH))) ||
                        (edgeSchema.getDestination().get().getLabel().get().equals(vertexLabel) &&
                                (context.getDirection().equals(Direction.IN) || context.getDirection().equals(Direction.BOTH))))
                .toJavaList();
    }
}
