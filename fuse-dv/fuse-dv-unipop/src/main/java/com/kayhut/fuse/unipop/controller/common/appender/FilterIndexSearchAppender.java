package com.kayhut.fuse.unipop.controller.common.appender;

/*-
 * #%L
 * fuse-dv-unipop
 * %%
 * Copyright (C) 2016 - 2018 yangdb   ------ www.yangdb.org ------
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import com.kayhut.fuse.unipop.controller.common.context.VertexControllerContext;
import com.kayhut.fuse.unipop.controller.search.SearchBuilder;
import com.kayhut.fuse.unipop.controller.utils.ElementUtil;
import com.kayhut.fuse.unipop.schemaProviders.GraphVertexSchema;
import com.kayhut.fuse.unipop.schemaProviders.indexPartitions.IndexPartitions;
import javaslang.collection.Stream;
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

        Iterable<GraphVertexSchema> vertexSchemas = context.getSchemaProvider().getVertexSchemas(vertexLabel);
        if (Stream.ofAll(vertexSchemas).isEmpty()) {
            return false;
        }

        // currently supports a single vertex schema
        GraphVertexSchema vertexSchema = Stream.ofAll(vertexSchemas).get(0);

        Optional<String> partitionField = vertexSchema.getIndexPartitions().get().getPartitionField().isPresent() ?
                    vertexSchema.getIndexPartitions().get().getPartitionField().get().equals("_id") ?
                        Optional.of(T.id.getAccessor()) :
                        Optional.of(vertexSchema.getIndexPartitions().get().getPartitionField().get()) :
                Optional.empty();

        boolean isPartitionFieldFullyAvailable = partitionField.isPresent() ?
                Stream.ofAll(context.getBulkVertices())
                        .map(vertex -> ElementUtil.value(vertex, partitionField.get()))
                        .filter(value -> !value.isPresent())
                        .size() == 0 :
                false;

        List<IndexPartitions.Partition.Range> rangePartitions =
                Stream.ofAll(vertexSchema.getIndexPartitions().get().getPartitions())
                        .filter(partition -> partition instanceof IndexPartitions.Partition.Range)
                        .map(partition -> (IndexPartitions.Partition.Range) partition)
                        .<Comparable>sortBy(partition -> (Comparable) partition.getTo())
                        .toJavaList();

        Iterable<IndexPartitions.Partition.Range> relevantRangePartitions = rangePartitions;
        if (isPartitionFieldFullyAvailable) {
            List<Comparable> partitionValues = Stream.ofAll(context.getBulkVertices())
                    .map(vertex -> ElementUtil.value(vertex, partitionField.get()))
                    .filter(Optional::isPresent)
                    .map(value -> (Comparable) value.get())
                    .distinct().sorted().toJavaList();

            relevantRangePartitions = findRelevantRangePartitions(rangePartitions, partitionValues);
        }

        Set<String> indices =
                Stream.ofAll(vertexSchema.getIndexPartitions().get().getPartitions())
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
