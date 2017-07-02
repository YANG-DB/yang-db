package com.kayhut.fuse.unipop.controller.search.appender;

import com.google.common.collect.Lists;
import com.kayhut.fuse.unipop.controller.context.PromiseElementControllerContext;
import com.kayhut.fuse.unipop.controller.search.SearchBuilder;
import com.kayhut.fuse.unipop.controller.utils.traversal.TraversalValuesByKeyProvider;
import com.kayhut.fuse.unipop.promise.TraversalConstraint;
import com.kayhut.fuse.unipop.schemaProviders.GraphEdgeSchema;
import com.kayhut.fuse.unipop.schemaProviders.GraphElementSchemaProvider;
import com.kayhut.fuse.unipop.schemaProviders.GraphVertexSchema;
import com.kayhut.fuse.unipop.schemaProviders.indexPartitions.IndexPartition;
import com.kayhut.fuse.unipop.structure.ElementType;
import org.apache.tinkerpop.gremlin.process.traversal.Traversal;
import org.apache.tinkerpop.gremlin.structure.T;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Created by User on 27/03/2017.
 */
public class IndexSearchAppender implements SearchAppender<PromiseElementControllerContext> {

    //region SearchAppender Implementation
    @Override
    public boolean append(SearchBuilder searchBuilder, PromiseElementControllerContext promiseElementControllerContext) {
        GraphElementSchemaProvider schemaProvider = promiseElementControllerContext.getSchemaProvider();
        Optional<TraversalConstraint> constraint = promiseElementControllerContext.getConstraint();
        if (!constraint.isPresent()) {
            manageSpecialCase(promiseElementControllerContext,schemaProvider,searchBuilder);
        } else {
            Traversal traversal = constraint.get().getTraversal();
            TraversalValuesByKeyProvider traversalValuesByKeyProvider = new TraversalValuesByKeyProvider();
            Set<String> labels = traversalValuesByKeyProvider.getValueByKey(traversal, T.label.getAccessor());
            if (!labels.isEmpty()) {
                labels.stream().forEach(label -> {
                    if (promiseElementControllerContext.getElementType() == ElementType.edge) {
                        Optional<Iterable<GraphEdgeSchema>> edgeSchemas = schemaProvider.getEdgeSchemas(label);
                        if (edgeSchemas.isPresent()) {
                            searchBuilder.getIndices().addAll(getEdgeSchemasIndices(edgeSchemas.get()));
                        }
                    } else if (promiseElementControllerContext.getElementType() == ElementType.vertex) {
                        Optional<GraphVertexSchema> vertexSchema = schemaProvider.getVertexSchema(label);
                        if (vertexSchema.isPresent()) {
                            searchBuilder.getIndices().addAll(getVertexSchemasIndices(vertexSchema));
                        }
                    }
                });
                return true;
            } else // No specific label - append all index partitions filtered by the type of the element (vertex or edge)
            {
                manageSpecialCase(promiseElementControllerContext,schemaProvider,searchBuilder);
            }
        }

        return true;
    }

    private void manageSpecialCase(PromiseElementControllerContext promiseElementControllerContext, GraphElementSchemaProvider schemaProvider, SearchBuilder searchBuilder) {
        if (promiseElementControllerContext.getElementType() == ElementType.vertex) {
            Iterable<String> vertexTypes = schemaProvider.getVertexTypes();
            vertexTypes.forEach(vertexType -> {
                Optional<GraphVertexSchema> vertexSchema = schemaProvider.getVertexSchema(vertexType);
                if (vertexSchema.isPresent()) {
                    searchBuilder.getIndices().addAll(getVertexSchemasIndices(vertexSchema));
                }
            });
        } else if (promiseElementControllerContext.getElementType() == ElementType.edge) {
            Iterable<String> edgeTypes = schemaProvider.getEdgeTypes();
            edgeTypes.forEach(edgeType -> {
                Optional<Iterable<GraphEdgeSchema>> edgeSchemas = schemaProvider.getEdgeSchemas(edgeType);
                if (edgeSchemas.isPresent()) {
                    searchBuilder.getIndices().addAll(getEdgeSchemasIndices(edgeSchemas.get()));
                }
            });
        }

    }

    //endregion

    //region Private Methods

    private List<String> getEdgeSchemasIndices(Iterable<GraphEdgeSchema> edgeSchemas) {
        ArrayList<String> indices = Lists.newArrayList();
        edgeSchemas.forEach(graphEdgeSchema ->
        {
            IndexPartition indexPartition = graphEdgeSchema.getIndexPartition();
            indices.addAll(Lists.newArrayList(indexPartition.getIndices()));
        });
        return indices;
    }

    private List<String> getVertexSchemasIndices(Optional<GraphVertexSchema> vertexSchema) {
        IndexPartition indexPartition = vertexSchema.get().getIndexPartition();
        return Lists.newArrayList(indexPartition.getIndices());
    }

    //endregion
}
