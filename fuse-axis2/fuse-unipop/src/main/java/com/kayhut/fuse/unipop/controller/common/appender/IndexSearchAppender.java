package com.kayhut.fuse.unipop.controller.common.appender;

import com.google.common.collect.Lists;
import com.kayhut.fuse.unipop.controller.common.context.ElementControllerContext;
import com.kayhut.fuse.unipop.controller.search.SearchBuilder;
import com.kayhut.fuse.unipop.controller.utils.traversal.TraversalValuesByKeyProvider;
import com.kayhut.fuse.unipop.promise.TraversalConstraint;
import com.kayhut.fuse.unipop.schemaProviders.GraphEdgeSchema;
import com.kayhut.fuse.unipop.schemaProviders.GraphElementSchema;
import com.kayhut.fuse.unipop.schemaProviders.GraphElementSchemaProvider;
import com.kayhut.fuse.unipop.schemaProviders.GraphVertexSchema;
import com.kayhut.fuse.unipop.schemaProviders.indexPartitions.IndexPartitions;
import com.kayhut.fuse.unipop.structure.ElementType;
import javaslang.collection.Stream;
import org.apache.tinkerpop.gremlin.process.traversal.Traversal;
import org.apache.tinkerpop.gremlin.structure.T;

import java.util.*;

/**
 * Created by User on 27/03/2017.
 */

// This appender will add indices to the search builder based on the elements IndexPartitions only.
public class IndexSearchAppender implements SearchAppender<ElementControllerContext> {

    //region SearchAppender Implementation
    @Override
    public boolean append(SearchBuilder searchBuilder, ElementControllerContext context) {
        GraphElementSchemaProvider schemaProvider = context.getSchemaProvider();
        Optional<TraversalConstraint> constraint = context.getConstraint();
        if (!constraint.isPresent()) {
            manageSpecialCase(context, schemaProvider, searchBuilder);
        } else {
            Traversal traversal = constraint.get().getTraversal();
            TraversalValuesByKeyProvider traversalValuesByKeyProvider = new TraversalValuesByKeyProvider();
            Set<String> labels = traversalValuesByKeyProvider.getValueByKey(traversal, T.label.getAccessor());
            if (!labels.isEmpty()) {
                labels.stream().forEach(label -> {
                    if (context.getElementType() == ElementType.edge) {
                        Iterable<GraphEdgeSchema> edgeSchemas = schemaProvider.getEdgeSchemas(label);
                        searchBuilder.getIndices().addAll(getEdgeSchemasIndices(edgeSchemas));
                    } else if (context.getElementType() == ElementType.vertex) {
                        Optional<GraphVertexSchema> vertexSchema = schemaProvider.getVertexSchema(label);
                        if (vertexSchema.isPresent()) {
                            searchBuilder.getIndices().addAll(getVertexSchemasIndices(vertexSchema));
                        }
                    }
                });
                return true;
            } else // No specific label - append all index partitions filtered by the type of the element (vertex or edge)
            {
                manageSpecialCase(context, schemaProvider, searchBuilder);
            }
        }

        return true;
    }

    private void manageSpecialCase(ElementControllerContext context, GraphElementSchemaProvider schemaProvider, SearchBuilder searchBuilder) {
        if (context.getElementType() == ElementType.vertex) {
            Iterable<String> vertexTypes = schemaProvider.getVertexLabels();
            vertexTypes.forEach(vertexType -> {
                Optional<GraphVertexSchema> vertexSchema = schemaProvider.getVertexSchema(vertexType);
                if (vertexSchema.isPresent()) {
                    searchBuilder.getIndices().addAll(getVertexSchemasIndices(vertexSchema));
                }
            });
        } else if (context.getElementType() == ElementType.edge) {
            Iterable<String> edgeTypes = schemaProvider.getEdgeLabels();
            edgeTypes.forEach(edgeType -> {
                Iterable<GraphEdgeSchema> edgeSchemas = schemaProvider.getEdgeSchemas(edgeType);
                    searchBuilder.getIndices().addAll(getEdgeSchemasIndices(edgeSchemas));
            });
        }

    }

    //endregion

    //region Private Methods

    private Collection<String> getEdgeSchemasIndices(Iterable<GraphEdgeSchema> edgeSchemas) {
        return Stream.ofAll(edgeSchemas)
                .filter(edgeSchema -> edgeSchema.getIndexPartitions().isPresent())
                .flatMap(edgeSchema -> edgeSchema.getIndexPartitions().get().partitions())
                .flatMap(IndexPartitions.Partition::indices)
                .toJavaSet();
    }

    private Collection<String> getVertexSchemasIndices(Optional<GraphVertexSchema> vertexSchema) {
        if (!vertexSchema.get().getIndexPartitions().isPresent()) {
            return Collections.emptyList();
        }

        return Stream.ofAll(vertexSchema.get().getIndexPartitions().get().partitions())
                .flatMap(IndexPartitions.Partition::indices)
                .toJavaSet();
    }

    //endregion
}
