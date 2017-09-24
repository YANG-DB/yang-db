package com.kayhut.fuse.unipop.controller.discrete.appender;

import com.kayhut.fuse.unipop.controller.common.appender.SearchAppender;
import com.kayhut.fuse.unipop.controller.common.context.VertexControllerContext;
import com.kayhut.fuse.unipop.controller.discrete.util.SchemaUtil;
import com.kayhut.fuse.unipop.controller.search.SearchBuilder;
import com.kayhut.fuse.unipop.controller.utils.ElementUtil;
import com.kayhut.fuse.unipop.schemaProviders.GraphEdgeSchema;
import com.kayhut.fuse.unipop.schemaProviders.indexPartitions.IndexPartitions;
import javaslang.collection.Stream;
import org.apache.tinkerpop.gremlin.structure.Direction;
import org.apache.tinkerpop.gremlin.structure.T;

import java.util.List;
import java.util.Set;

/**
 * Created by roman.margolis on 24/09/2017.
 */
public class SingularEdgeIndexSearchAppender implements SearchAppender<VertexControllerContext> {
    //region SearchAppender Implementation
    @Override
    public boolean append(SearchBuilder searchBuilder, VertexControllerContext context) {
        Iterable<GraphEdgeSchema> edgeSchemas = SchemaUtil.getRelevantSingularEdgeSchemas(context);
        if (Stream.ofAll(edgeSchemas).isEmpty()) {
            return false;
        }

        //currently assuming only one schema
        GraphEdgeSchema edgeSchema = Stream.ofAll(edgeSchemas).get(0);

        if (edgeSchema.getIndexPartitions().isPresent()) {
            return false;
        }

        GraphEdgeSchema.End endSchema = context.getDirection().equals(Direction.OUT) ?
                edgeSchema.getSource().get() :
                edgeSchema.getDestination().get();

        if (!endSchema.getIndexPartitions().isPresent()) {
            return false;
        }

        String partitionField = endSchema.getIndexPartitions().get().partitionField().get().equals("_id") ?
                T.id.getAccessor() :
                endSchema.getIndexPartitions().get().partitionField().get();

        List<Object> partitionValues = Stream.ofAll(context.getBulkVertices())
                .map(vertex -> ElementUtil.value(vertex, partitionField))
                .distinct().sorted().toJavaList();

        List<IndexPartitions.Partition.Range> rangePartitions =
                Stream.ofAll(endSchema.getIndexPartitions().get().partitions())
                    .filter(partition -> partition instanceof IndexPartitions.Partition.Range)
                    .map(partition -> (IndexPartitions.Partition.Range)partition)
                    .<Comparable>sortBy(partition -> (Comparable)partition.to())
                    .toJavaList();

        return false;
    }
    //endregion
}
