package com.kayhut.fuse.unipop.controller.discrete.appender;

import com.kayhut.fuse.unipop.controller.common.appender.SearchAppender;
import com.kayhut.fuse.unipop.controller.common.context.VertexControllerContext;
import com.kayhut.fuse.unipop.controller.utils.EdgeSchemaSupplier;
import com.kayhut.fuse.unipop.controller.search.SearchBuilder;
import com.kayhut.fuse.unipop.controller.utils.ElementUtil;
import com.kayhut.fuse.unipop.schemaProviders.GraphEdgeSchema;
import com.kayhut.fuse.unipop.schemaProviders.indexPartitions.IndexPartitions;
import javaslang.collection.Stream;
import org.apache.tinkerpop.gremlin.structure.Direction;
import org.apache.tinkerpop.gremlin.structure.T;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by roman.margolis on 24/09/2017.
 */
public class SingularEdgeIndexSearchAppender implements SearchAppender<VertexControllerContext> {
    //region SearchAppender Implementation
    @Override
    public boolean append(SearchBuilder searchBuilder, VertexControllerContext context) {
        Iterable<GraphEdgeSchema> edgeSchemas = new EdgeSchemaSupplier(context).labels().singular().applicable().get();
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

        List<Comparable> partitionValues = Stream.ofAll(context.getBulkVertices())
                .map(vertex -> (Comparable)ElementUtil.value(vertex, partitionField))
                .distinct().sorted().toJavaList();

        List<IndexPartitions.Partition.Range> rangePartitions =
                Stream.ofAll(endSchema.getIndexPartitions().get().partitions())
                    .filter(partition -> partition instanceof IndexPartitions.Partition.Range)
                    .map(partition -> (IndexPartitions.Partition.Range)partition)
                    .<Comparable>sortBy(partition -> (Comparable)partition.to())
                    .toJavaList();

        Iterable<IndexPartitions.Partition.Range> relevantRangePartitions = findRelevantRangePartitions(rangePartitions, partitionValues);
        Set<String> indices =
                Stream.ofAll(endSchema.getIndexPartitions().get().partitions())
                .filter(partition -> !(partition instanceof IndexPartitions.Partition.Range))
                .appendAll(relevantRangePartitions)
                .flatMap(IndexPartitions.Partition::indices)
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
            } else if (partition.to().compareTo(value) < 0) {
                partitionIndex++;
            } else {
                valueIndex++;
            }
        }

        return foundPartitions;
    }
    //endregion
}
