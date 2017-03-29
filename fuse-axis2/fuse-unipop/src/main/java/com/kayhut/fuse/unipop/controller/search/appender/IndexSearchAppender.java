package com.kayhut.fuse.unipop.controller.search.appender;

import com.google.common.collect.Lists;
import com.google.common.collect.Streams;
import com.kayhut.fuse.unipop.controller.search.SearchBuilder;
import com.kayhut.fuse.unipop.controller.context.PromiseElementControllerContext;
import com.kayhut.fuse.unipop.controller.utils.TraversalValuesByKeyProvider;
import com.kayhut.fuse.unipop.promise.Constraint;
import com.kayhut.fuse.unipop.schemaProviders.*;
import com.kayhut.fuse.unipop.schemaProviders.indexPartitions.IndexPartition;
import com.kayhut.fuse.unipop.structure.ElementType;
import org.apache.tinkerpop.gremlin.process.traversal.Traversal;

import java.util.*;

import static java.util.Collections.emptyIterator;

/**
 * Created by User on 27/03/2017.
 */
public class IndexSearchAppender implements SearchAppender<PromiseElementControllerContext> {

    //region SearchAppender Implementation
    @Override
    public boolean append(SearchBuilder searchBuilder, PromiseElementControllerContext promiseElementControllerContext) {
        try {
            GraphElementSchemaProvider schemaProvider = promiseElementControllerContext.getSchemaProvider();
            Traversal traversal = promiseElementControllerContext.getConstraint().get().getTraversal();
            TraversalValuesByKeyProvider traversalValuesByKeyProvider = new TraversalValuesByKeyProvider();
            Set<String> labels = traversalValuesByKeyProvider.getValueByKey(traversal, "label");

            if (!labels.isEmpty()) {
                labels.stream().forEach(label -> {
                    if (promiseElementControllerContext.getElementType() == ElementType.edge) {
                        Optional<Iterable<GraphEdgeSchema>> edgeSchemas = schemaProvider.getEdgeSchemas(label);
                        if (edgeSchemas.isPresent()) {
                            searchBuilder.getIndices().addAll(getEdgeSchemasIndices(edgeSchemas.get()));
                        }
                    }
                    else if (promiseElementControllerContext.getElementType() == ElementType.vertex)
                    {
                        Optional<GraphVertexSchema> vertexSchema = schemaProvider.getVertexSchema(label);
                        if(vertexSchema.isPresent()) {
                            searchBuilder.getIndices().addAll(getVertexSchemasIndices(vertexSchema));
                        }
                    }
                });
                return true;
            }
            else // No specific label - search in all index partitions filtered by the type of the element (vertex or edge)
            {
                if (promiseElementControllerContext.getElementType() == ElementType.vertex) {
                    Iterable<String> vertexTypes = schemaProvider.getVertexTypes();
                    vertexTypes.forEach(vertexType -> {
                        Optional<GraphVertexSchema> vertexSchema = schemaProvider.getVertexSchema(vertexType);
                        if(vertexSchema.isPresent()) {
                            searchBuilder.getIndices().addAll(getVertexSchemasIndices(vertexSchema));
                        }
                    });
                }
                else if (promiseElementControllerContext.getElementType() == ElementType.edge) {
                    Iterable<String> edgeTypes = schemaProvider.getEdgeTypes();
                    edgeTypes.forEach(edgeType -> {
                        Optional<Iterable<GraphEdgeSchema>> edgeSchemas = schemaProvider.getEdgeSchemas(edgeType);
                        if (edgeSchemas.isPresent()){
                            searchBuilder.getIndices().addAll(getEdgeSchemasIndices(edgeSchemas.get()));
                        }
                    });
                }
            }
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    //endregion

    //region Private Methods

    private List<String> getEdgeSchemasIndices(Iterable<GraphEdgeSchema> edgeSchemas) {
        ArrayList<String> indices = Lists.newArrayList();
        edgeSchemas.forEach(graphEdgeSchema ->
        {
            Iterable<IndexPartition> indexPartitions = graphEdgeSchema.getIndexPartitions();
            indexPartitions.forEach(indexPartition -> {
                indices.addAll(Lists.newArrayList(indexPartition.getIndices()));
            });
        });
        return indices;
    }

    private List<String> getVertexSchemasIndices(Optional<GraphVertexSchema> vertexSchema) {
        ArrayList<String> indices = Lists.newArrayList();
        Iterable<IndexPartition> indexPartitions = vertexSchema.get().getIndexPartitions();
        indexPartitions.forEach(indexPartition -> {
            indices.addAll(Lists.newArrayList(indexPartition.getIndices()));
        });
        return indices;
    }

    //endregion
}
