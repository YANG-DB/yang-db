package com.kayhut.fuse.unipop.controller.common.appender;

import com.kayhut.fuse.unipop.controller.common.context.CompositeControllerContext;
import com.kayhut.fuse.unipop.controller.common.context.VertexControllerContext;
import com.kayhut.fuse.unipop.controller.promise.context.PromiseVertexFilterControllerContext;
import com.kayhut.fuse.unipop.controller.search.SearchBuilder;
import com.kayhut.fuse.unipop.controller.utils.ElementUtil;
import com.kayhut.fuse.unipop.promise.IdPromise;
import com.kayhut.fuse.unipop.schemaProviders.GraphVertexSchema;
import com.kayhut.fuse.unipop.schemaProviders.indexPartitions.IndexPartitions;
import com.kayhut.fuse.unipop.structure.promise.PromiseVertex;
import javaslang.collection.Stream;
import org.apache.tinkerpop.gremlin.structure.Element;
import org.apache.tinkerpop.gremlin.structure.T;
import org.unipop.schema.element.VertexSchema;

import java.util.*;

/**
 * Created by Roman on 12/06/2017.
 */
public class FilterIndexSearchAppender implements SearchAppender<VertexControllerContext> {
    //region SearchAppender Implementation
    @Override
    public boolean append(SearchBuilder searchBuilder, VertexControllerContext context) {
        // currently assuming homogeneous vertex bulk
        String vertexLabel = Stream.ofAll(context.getBulkVertices()).get(0).label();

        Optional<GraphVertexSchema> vertexSchema = context.getSchemaProvider().getVertexSchema(vertexLabel);
        if (!vertexSchema.isPresent()) {
            return false;
        }

        String partitionField = vertexSchema.get().getIndexPartitions().get().getPartitionField().get().equals("_id") ?
                T.id.getAccessor() :
                vertexSchema.get().getIndexPartitions().get().getPartitionField().get();

        boolean isPartitionFieldFullyAvailable =
                Stream.ofAll(context.getBulkVertices())
                        .map(vertex -> ElementUtil.value(vertex, partitionField))
                        .filter(value -> !value.isPresent())
                        .size() == 0;

        List<IndexPartitions.Partition.Range> rangePartitions =
                Stream.ofAll(vertexSchema.get().getIndexPartitions().get().getPartitions())
                        .filter(partition -> partition instanceof IndexPartitions.Partition.Range)
                        .map(partition -> (IndexPartitions.Partition.Range) partition)
                        .<Comparable>sortBy(partition -> (Comparable) partition.getTo())
                        .toJavaList();

        Iterable<IndexPartitions.Partition.Range> relevantRangePartitions = rangePartitions;
        if (isPartitionFieldFullyAvailable) {
            List<Comparable> partitionValues = Stream.ofAll(context.getBulkVertices())
                    .map(vertex -> ElementUtil.value(vertex, partitionField))
                    .filter(Optional::isPresent)
                    .map(value -> (Comparable) value.get())
                    .distinct().sorted().toJavaList();

            relevantRangePartitions = findRelevantRangePartitions(rangePartitions, partitionValues);
        }

        Set<String> indices =
                Stream.ofAll(vertexSchema.get().getIndexPartitions().get().getPartitions())
                        .filter(partition -> !(partition instanceof IndexPartitions.Partition.Range))
                        .appendAll(relevantRangePartitions)
                        .flatMap(IndexPartitions.Partition::getIndices)
                        .toJavaSet();

        searchBuilder.getIndices().addAll(indices);
        return indices.size() > 0;
    }
    //endregion

    //Private Methods
    private Set<IndexPartitions.Partition.Range> findRelevantRangePartitions(
            List<IndexPartitions.Partition.Range> partitions,
            List<Comparable> values) {
        Set<IndexPartitions.Partition.Range> foundPartitions = new HashSet<>();
        int partitionIndex = 0;
        int valueIndex = 0;

        while(partitionIndex < partitions.size() && valueIndex < values.size()) {
            IndexPartitions.Partition.Range partition = partitions.get(partitionIndex);
            Comparable value = values.get(valueIndex);

            if (partition.isWithin(value)) {
                foundPartitions.add(partition);
                valueIndex++;
            } else if (partition.getTo().compareTo(value) < 0) {
                partitionIndex++;
            } else {
                valueIndex++;
            }
        }

        return foundPartitions;
    }
    //endregion
}
