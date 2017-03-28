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

import java.util.ArrayList;
import java.util.Optional;
import java.util.Set;

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
                    if (promiseElementControllerContext.getElementType() == ElementType.vertex) {
                        Optional<Iterable<GraphEdgeSchema>> edgeSchemas = schemaProvider.getEdgeSchemas(label);
                        if (edgeSchemas.isPresent()) {
                            edgeSchemas.get().forEach(graphEdgeSchema ->
                            {
                                Iterable<IndexPartition> indexPartitions = graphEdgeSchema.getIndexPartitions();
                                indexPartitions.forEach(indexPartition -> {
                                    searchBuilder.getIndices().addAll(Lists.newArrayList(indexPartition.getIndices()));
                                });
                            });
                        }
                    }
                    else if (promiseElementControllerContext.getElementType() == ElementType.edge)
                    {
                        Optional<GraphVertexSchema> vertexSchema = schemaProvider.getVertexSchema(label);
                        if(vertexSchema.isPresent()) {
                            Iterable<IndexPartition> indexPartitions = vertexSchema.get().getIndexPartitions();
                            indexPartitions.forEach(indexPartition -> {
                                searchBuilder.getIndices().addAll(Lists.newArrayList(indexPartition.getIndices()));
                            });
                        }
                    }
                });
                return true;
            }
        } catch (Exception e) {
            return false;
        }
        return false;
    }
    //endregion
}
