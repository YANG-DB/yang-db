package com.yangdb.fuse.unipop.controller.promise.appender;

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

import com.yangdb.fuse.unipop.controller.common.appender.SearchAppender;
import com.yangdb.fuse.unipop.controller.common.context.VertexControllerContext;
import com.yangdb.fuse.unipop.controller.search.SearchBuilder;
import com.yangdb.fuse.unipop.controller.utils.CollectionUtil;
import com.yangdb.fuse.unipop.controller.utils.traversal.TraversalPredicateByKeyProvider;
import com.yangdb.fuse.unipop.controller.utils.traversal.TraversalValuesByKeyProvider;
import com.yangdb.fuse.unipop.schemaProviders.GraphEdgeSchema;
import com.yangdb.fuse.unipop.schemaProviders.indexPartitions.IndexPartitions;
import com.yangdb.fuse.unipop.schemaProviders.indexPartitions.TimeSeriesBuckets;
import com.yangdb.fuse.unipop.schemaProviders.indexPartitions.TimeSeriesIndexPartitions;
import javaslang.collection.Stream;
import org.apache.tinkerpop.gremlin.process.traversal.Compare;
import org.apache.tinkerpop.gremlin.process.traversal.Contains;
import org.apache.tinkerpop.gremlin.process.traversal.P;
import org.apache.tinkerpop.gremlin.process.traversal.util.AndP;
import org.apache.tinkerpop.gremlin.process.traversal.util.OrP;
import org.apache.tinkerpop.gremlin.structure.T;

import java.util.Comparator;
import java.util.Date;
import java.util.Optional;
import java.util.Set;

/**
 * Created by Elad on 4/26/2017.
 */
public class PromiseEdgeIndexAppender implements SearchAppender<VertexControllerContext> {

    @Override
    public boolean append(SearchBuilder searchBuilder, VertexControllerContext context) {

        TraversalValuesByKeyProvider labelsProvider = new TraversalValuesByKeyProvider();
        Set<String> labels = labelsProvider.getValueByKey(context.getConstraint().get().getTraversal(), T.label.getAccessor());
        for (String label :labels) {
            Iterable<GraphEdgeSchema> edgeSchemas = context.getSchemaProvider().getEdgeSchemas(label);
            if (!Stream.ofAll(edgeSchemas).isEmpty()) {
                // currently supports a single edge schema
                GraphEdgeSchema edgeSchema = Stream.ofAll(edgeSchemas).get(0);
                IndexPartitions indexPartitions = edgeSchema.getIndexPartitions().get();
                if (indexPartitions instanceof TimeSeriesIndexPartitions) {
                    TimeSeriesIndexPartitions tsIndexPartition = (TimeSeriesIndexPartitions) indexPartitions;
                    TraversalPredicateByKeyProvider visitor = new TraversalPredicateByKeyProvider();
                    Set<P> predicates = visitor.getPredicateByKey(context.getConstraint().get().getTraversal(), tsIndexPartition.getTimeField());
                    if(predicates.size() == 0) {
                        //if there are no constraints, add all getIndices
                        searchBuilder.getIndices().addAll(Stream.ofAll(indexPartitions.getPartitions()).flatMap(IndexPartitions.Partition::getIndices).toJavaSet());
                    } else {
                        //ass only getIndices satisfying the constraints
                        searchBuilder.getIndices().addAll(Stream.ofAll(indexPartitions.getPartitions())
                            .flatMap(IndexPartitions.Partition::getIndices)
                                .filter(index -> isIndexRelevant(index, predicates, tsIndexPartition))
                                .toJavaSet());
                    }
                } else {
                    //index partition is static, add all getIndices
                    searchBuilder.getIndices().addAll(Stream.ofAll(indexPartitions.getPartitions()).flatMap(IndexPartitions.Partition::getIndices).toJavaSet());
                }
            }
        }
        return true;

    }

    private boolean isIndexRelevant(String index, Set<P> predicates, TimeSeriesIndexPartitions tsIndexPartition) {

        for (P predicate : predicates) {
            if(predicate instanceof AndP) {
                return Stream.ofAll(((AndP) predicate).getPredicates()).find(p -> !isIndexRelevant(index, Stream.of((P) p).toJavaSet(), tsIndexPartition)).isEmpty();
            } else if (predicate instanceof OrP) {
                return Stream.ofAll(((OrP) predicate).getPredicates()).find(p -> isIndexRelevant(index, Stream.of((P) p).toJavaSet(), tsIndexPartition)).toJavaList().size() > 0;
            } else {
                return testSimplePredicate(predicate, index, tsIndexPartition);
            }
        }

        return true;
    }

    /**
     * partition values are range buckets (see {@link TimeSeriesBuckets}): an index is relevant when its bucket
     * can hold a value matching the predicate; buckets are compared numerically, never as text
     */
    private boolean testSimplePredicate(P predicate, String index, TimeSeriesIndexPartitions tsIndexPartition) {
        Comparator<String> byBucket = TimeSeriesBuckets.byBucket(tsIndexPartition.getIndexFormat());

        if (predicate.getBiPredicate().equals(Compare.eq)) {
            Optional<String> bucket = getIndexName(predicate.getValue(), tsIndexPartition);
            // an unknown value type cannot be bucketed - keep the index
            return bucket == null || (bucket.isPresent() && byBucket.compare(index, bucket.get()) == 0);

        } else if (predicate.getBiPredicate().equals(Compare.gt) ||
                   predicate.getBiPredicate().equals(Compare.gte)) {
            // the value's bucket and every later one; nothing when the value is after the last bucket
            Optional<String> bucket = getIndexName(predicate.getValue(), tsIndexPartition);
            return bucket == null || (bucket.isPresent() && byBucket.compare(index, bucket.get()) >= 0);

        } else if (predicate.getBiPredicate().equals(Compare.lt) ||
                   predicate.getBiPredicate().equals(Compare.lte)) {
            // the value's bucket and every earlier one; all of them when the value is after the last bucket
            Optional<String> bucket = getIndexName(predicate.getValue(), tsIndexPartition);
            return bucket == null || !bucket.isPresent() || byBucket.compare(index, bucket.get()) <= 0;

        } else if (predicate.getBiPredicate().equals(Compare.neq)) {
            //all getIndices are relevant
            return true;
        } else if (predicate.getBiPredicate().equals(Contains.within)) {
            return Stream.ofAll(CollectionUtil.listFromObjectValue(predicate.getValue()))
                    .map(value -> getIndexName(value, tsIndexPartition))
                    .exists(bucket -> bucket == null || (bucket.isPresent() && byBucket.compare(index, bucket.get()) == 0));
        } else if (predicate.getBiPredicate().equals(Contains.without)) {
            //all getIndices are relevant ?
            return true;
        } else {
            //default ??
            return true;
        }
    }

    /**
     * @return the bucket covering the value, empty when the value is after the last bucket,
     * or null when the value is not a date
     */
    private Optional<String> getIndexName(Object value, TimeSeriesIndexPartitions indexPartition) {
        Date date;
        if (value instanceof Date) {
            date = (Date) value;
        } else if (value instanceof Number) {
            date = new Date(((Number) value).longValue());
        } else {
            return null;
        }
        return Optional.ofNullable(indexPartition.getIndexName(date));
    }

}
