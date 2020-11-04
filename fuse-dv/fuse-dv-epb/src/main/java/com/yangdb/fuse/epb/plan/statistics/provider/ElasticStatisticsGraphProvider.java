package com.yangdb.fuse.epb.plan.statistics.provider;

/*-
 * #%L
 * fuse-dv-epb
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

import com.github.benmanes.caffeine.cache.Cache;
import com.google.inject.Inject;
import com.yangdb.fuse.epb.plan.statistics.GraphStatisticsProvider;
import com.yangdb.fuse.epb.plan.statistics.Statistics;
import com.yangdb.fuse.epb.plan.statistics.configuration.StatConfig;
import com.yangdb.fuse.model.GlobalConstants;
import com.yangdb.fuse.model.query.properties.constraint.Constraint;
import com.yangdb.fuse.model.query.Rel;
import com.yangdb.fuse.unipop.controller.utils.traversal.TraversalValuesByKeyProvider;
import com.yangdb.fuse.unipop.schemaProviders.*;
import com.yangdb.fuse.unipop.schemaProviders.indexPartitions.IndexPartitions;
import javaslang.Tuple2;
import javaslang.collection.Stream;
import org.apache.tinkerpop.gremlin.structure.T;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by benishue on 22-May-17.
 */
public class ElasticStatisticsGraphProvider implements GraphStatisticsProvider {
    //region Static
    //todo move it getTo a global enum across modules
    private static final String DATE = "date";
    private static final String ENUM = "enum";
    private static final String INT = "int";
    private static final String FLOAT = "float";
    private static final String STRING = "string";

    private static final String FIELD_NAME_TYPE = "type";
    private static final String FIELD_NAME_EDGE = GlobalConstants.EdgeSchema.SOURCE_ID;
    //endregion

    //region Constructors
    @Inject
    public ElasticStatisticsGraphProvider(StatConfig config,
                                          ElasticStatProvider elasticStatProvider,
                                          Cache<Tuple2<String, List<String>>, List<Statistics.BucketInfo>> cache) {
        this.statConfig = config;
        this.elasticStatProvider = elasticStatProvider;
        this.cache = cache;
    }
    //endregion

    //region GraphStatisticsProvider Implementation
    @Override
    public Statistics.SummaryStatistics getVertexCardinality(GraphVertexSchema graphVertexSchema) {
        return getVertexCardinality(graphVertexSchema,
                Stream.ofAll(graphVertexSchema.getIndexPartitions().get().getPartitions())
                .flatMap(IndexPartitions.Partition::getIndices)
                .toJavaList());
    }

    @Override
    public Statistics.SummaryStatistics getVertexCardinality(GraphVertexSchema graphVertexSchema, List<String> relevantIndices) {
        String constraintLabel = Stream.ofAll(
                new TraversalValuesByKeyProvider().getValueByKey(graphVertexSchema.getConstraint().getTraversalConstraint(), T.label.getAccessor()))
                .get(0);

        return getSummaryStatistics(constraintLabel, relevantIndices);
    }

    @Override
    public Statistics.SummaryStatistics getEdgeCardinality(GraphEdgeSchema graphEdgeSchema) {
        return getEdgeCardinality(graphEdgeSchema,
                Stream.ofAll(graphEdgeSchema.getIndexPartitions().get().getPartitions())
                        .flatMap(IndexPartitions.Partition::getIndices)
                        .toJavaList());
    }

    @Override
    public Statistics.SummaryStatistics getEdgeCardinality(GraphEdgeSchema graphEdgeSchema, List<String> relevantIndices) {
        String constraintLabel = Stream.ofAll(
                new TraversalValuesByKeyProvider().getValueByKey(graphEdgeSchema.getConstraint().getTraversalConstraint(), T.label.getAccessor()))
                .get(0);

        return getSummaryStatistics(constraintLabel, relevantIndices);
    }

    @Override
    public <T extends Comparable<T>> Statistics.HistogramStatistics<T> getConditionHistogram(GraphElementSchema graphElementSchema,
                                                                                             List<String> relevantIndices,
                                                                                             GraphElementPropertySchema graphElementPropertySchema,
                                                                                             Constraint constraint,
                                                                                             Class<T> tp) {
        List<Statistics.HistogramStatistics<T>> histograms = new ArrayList<>();

        String statTypeName = getStatTypeName(graphElementPropertySchema);

        String constraintLabel = Stream.ofAll(
                new TraversalValuesByKeyProvider().getValueByKey(graphElementSchema.getConstraint().getTraversalConstraint(), org.apache.tinkerpop.gremlin.structure.T.label.getAccessor()))
                .get(0);

        Map<String, List<Statistics.BucketInfo>> fieldStatisticsPerIndex = this.elasticStatProvider.getFieldStatisticsPerIndex(
                relevantIndices,
                Collections.singletonList(constraintLabel),
                Collections.singletonList(((graphElementPropertySchema instanceof GraphRedundantPropertySchema) ?
                        ((GraphRedundantPropertySchema) graphElementPropertySchema).getPropertyRedundantName()
                        : graphElementPropertySchema.getName())));

        for (Map.Entry<String, List<Statistics.BucketInfo>> entry : fieldStatisticsPerIndex.entrySet()) {
            List<Statistics.BucketInfo> sortedBuckets = Stream.ofAll(entry.getValue())
                    .sorted((o1, o2) -> o1.getLowerBound().compareTo(o2.getLowerBound())).toJavaList();

            histograms.add(new Statistics.HistogramStatistics(
                    sortedBuckets
            ));
        }

        Statistics.HistogramStatistics<T> combine = Statistics.HistogramStatistics.combine(histograms);
        return transform(graphElementPropertySchema.getType(), combine);
    }

    /**
     * convert getTo specific type boundaries
     *
     * @param fieldType
     * @param statistics
     * @param <T>
     * @return
     */
    private <T extends Comparable<T>> Statistics.HistogramStatistics<T> transform(String fieldType, Statistics.HistogramStatistics<T> statistics) {
        switch (fieldType) {
            case STRING: { //String
                break;
            }
            case INT: { //Numeric
                break;
            }
            case ENUM: { //Enum
                break;
            }
            case DATE: { //Enum ? string //todo decide?
                List<Statistics.BucketInfo> collect = statistics.getBuckets().stream().map(b -> {
                    long lowerBound = (Long)b.getLowerBound();
                    long higherBound = (Long) b.getHigherBound();
                    return new Statistics.BucketInfo<>(b.getTotal(), b.getCardinality(), new Date(lowerBound), new Date(higherBound));
                }).collect(Collectors.toList());
                statistics = new Statistics.HistogramStatistics(collect);
                break;
            }
        }

        return statistics;
    }

    @Override
    public long getGlobalSelectivity(GraphEdgeSchema graphEdgeSchema,
                                     Rel.Direction direction,
                                     List<String> relevantIndices) {
        String constraintLabel = Stream.ofAll(
                new TraversalValuesByKeyProvider().getValueByKey(graphEdgeSchema.getConstraint().getTraversalConstraint(), org.apache.tinkerpop.gremlin.structure.T.label.getAccessor()))
                .get(0);

        long globalSelectivity = 0;
        List<Statistics.BucketInfo<String>> buckets = this.elasticStatProvider.getEdgeGlobalStatistics(
                relevantIndices,
                Collections.singletonList(constraintLabel),
                Collections.singletonList(FIELD_NAME_EDGE),
                convertDirection(direction)
        );

        if (!buckets.isEmpty()) {
            long total = Stream.ofAll(buckets).map(Statistics.BucketInfo::getTotal).sum().longValue();
            double cardinality = Stream.ofAll(buckets).map(Statistics.BucketInfo::getCardinality).average().get();
            globalSelectivity = Math.round(total / cardinality);
        }

        return globalSelectivity;
    }
    //endregion

    //region Private Methods
    private Statistics.SummaryStatistics getSummaryStatistics(String docType, List<String> indices) {
        long totalCount = Stream.ofAll(getBuckets(docType, indices))
                .map(Statistics.BucketInfo::getTotal)
                .sum().longValue();

        return new Statistics.SummaryStatistics(totalCount, 1);
    }

    private List<Statistics.BucketInfo> getBuckets(String docType, List<String> indices) {
        return cache.get(new Tuple2<>(docType, indices),
                stringListTuple2 -> elasticStatProvider.getFieldStatistics(
                        indices,
                        Collections.singletonList(docType),
                        Collections.singletonList(FIELD_NAME_TYPE)));
    }

    private String getStatTypeName(GraphElementPropertySchema propertySchema) {
        String p = propertySchema.getName();
        if (propertySchema instanceof GraphRedundantPropertySchema)
            p = ((GraphRedundantPropertySchema) propertySchema).getPropertyRedundantName();

        if (p.endsWith(".type"))
            return statConfig.getStatTermTypeName();

        String statTypeName;

        switch (propertySchema.getType()) {
            case STRING: { //String
                statTypeName = statConfig.getStatStringTypeName();
                break;
            }
            case INT: { //Numeric
                statTypeName = statConfig.getStatNumericTypeName();
                break;
            }
            case FLOAT: { //numeric
                statTypeName = statConfig.getStatNumericTypeName();
                break;
            }
            case ENUM: { //Enum
                statTypeName = statConfig.getStatTermTypeName();
                break;
            }
            case DATE: { //Enum ? string //todo decide?
                statTypeName = statConfig.getStatNumericTypeName();
                break;
            }
            default:
                throw new IllegalArgumentException("Field type is missing/invalid" + propertySchema.getType());
        }
        return statTypeName;
    }

    public String convertDirection(Rel.Direction dir) {
        switch (dir) {
            case R:
                return "OUT";
            case L:
                return "IN";
            default:
                throw new IllegalArgumentException("Not Supported Relation DirectionSchema: " + dir);
        }
    }
    //endregion

    //region Fields
    private final StatConfig statConfig;
    private final ElasticStatProvider elasticStatProvider;
    private final Cache<Tuple2<String, List<String>>, List<Statistics.BucketInfo>> cache;
    //endregion

}
