package com.yangdb.fuse.unipop.schemaProviders.indexPartitions;

/*-
 * #%L
 * fuse-dv-unipop
 * %%
 * Copyright (C) 2016 - 2019 The YangDb Graph Database Project
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

import java.util.Date;
import java.util.Optional;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public interface TimeSeriesIndexPartitions extends IndexPartitions {
    String getDateFormat();
    String getIndexPrefix();
    String getIndexFormat();
    String getTimeField();

    /**
     * the index of the range bucket covering the date (see {@link TimeSeriesBuckets} for the bucket semantics),
     * or null when the date is after the last bucket
     */
    String getIndexName(Date date);

    /**
     * the same time partitioning with every index name passed through mapIndex (e.g. adding a prefix);
     * schema decorators must use this rather than rebuilding a plain {@link IndexPartitions}, which would lose
     * the time buckets and send every document to the first index
     */
    static TimeSeriesIndexPartitions mapIndices(TimeSeriesIndexPartitions source, UnaryOperator<String> mapIndex) {
        return new TimeSeriesIndexPartitions() {
            @Override
            public String getDateFormat() {
                return source.getDateFormat();
            }

            @Override
            public String getIndexPrefix() {
                return source.getIndexPrefix() == null ? null : mapIndex.apply(source.getIndexPrefix());
            }

            @Override
            public String getIndexFormat() {
                return source.getIndexFormat() == null ? null : mapIndex.apply(source.getIndexFormat());
            }

            @Override
            public String getTimeField() {
                return source.getTimeField();
            }

            @Override
            public String getIndexName(Date date) {
                String indexName = source.getIndexName(date);
                return indexName == null ? null : mapIndex.apply(indexName);
            }

            @Override
            public Optional<String> getPartitionField() {
                return source.getPartitionField();
            }

            @Override
            public Iterable<Partition> getPartitions() {
                return StreamSupport.stream(source.getPartitions().spliterator(), false)
                        .map(partition -> (Partition) () -> StreamSupport.stream(partition.getIndices().spliterator(), false)
                                .map(mapIndex)
                                .collect(Collectors.toList()))
                        .collect(Collectors.toList());
            }
        };
    }
}
