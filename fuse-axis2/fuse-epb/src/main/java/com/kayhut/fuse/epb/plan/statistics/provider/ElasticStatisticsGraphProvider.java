package com.kayhut.fuse.epb.plan.statistics.provider;

import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.kayhut.fuse.epb.plan.statistics.GraphStatisticsProvider;
import com.kayhut.fuse.epb.plan.statistics.Statistics;
import com.kayhut.fuse.epb.plan.statistics.configuration.StatConfig;
import com.kayhut.fuse.epb.plan.statistics.util.StatUtil;
import com.kayhut.fuse.model.query.Constraint;
import com.kayhut.fuse.model.query.Rel;
import com.kayhut.fuse.unipop.schemaProviders.GraphEdgeSchema;
import com.kayhut.fuse.unipop.schemaProviders.GraphElementPropertySchema;
import com.kayhut.fuse.unipop.schemaProviders.GraphElementSchema;
import com.kayhut.fuse.unipop.schemaProviders.GraphVertexSchema;
import javaslang.collection.Stream;
import org.elasticsearch.client.transport.TransportClient;

import java.util.*;

/**
 * Created by benishue on 22-May-17.
 */
public class ElasticStatisticsGraphProvider implements GraphStatisticsProvider {

    //todo move it to a global enum across modules
    private static final String DATE = "date";
    private static final String ENUM = "enum";
    private static final String INT = "int";
    private static final String STRING = "string";

    private static final String FIELD_NAME_TYPE = "_type";
    private static final String FIELD_NAME_EDGE = "entityA.id";
    private static final String FIELD_NAME_TERM = "term";

    //region Constructors
    @Inject
    public ElasticStatisticsGraphProvider(StatConfig config) {
        this.statConfig = config;
        this.elasticStatProvider = new ElasticStatProvider(config);
        this.elasticClient = new ElasticClientProvider(config).getStatClient();
    }
    //endregion

    //region GraphStatisticsProvider Implementation
    @Override
    public Statistics.SummaryStatistics getVertexCardinality(GraphVertexSchema graphVertexSchema) {
        return getVertexCardinality(graphVertexSchema,
                Lists.newArrayList(graphVertexSchema.getIndexPartition().getIndices()));
    }

    @Override
    public Statistics.SummaryStatistics getVertexCardinality(GraphVertexSchema graphVertexSchema, List<String> relevantIndices) {
        return getStatResultsForType(graphVertexSchema.getType(), relevantIndices);
    }

    @Override
    public Statistics.SummaryStatistics getEdgeCardinality(GraphEdgeSchema graphEdgeSchema) {
        return getEdgeCardinality(graphEdgeSchema,
                Lists.newArrayList(graphEdgeSchema.getIndexPartition().getIndices()));
    }

    @Override
    public Statistics.SummaryStatistics getEdgeCardinality(GraphEdgeSchema graphEdgeSchema, List<String> relevantIndices) {
        return getStatResultsForType(graphEdgeSchema.getType(), relevantIndices);
    }

    @Override
    public <T extends Comparable<T>> Statistics.HistogramStatistics<T> getConditionHistogram(GraphElementSchema graphElementSchema,
                                                                                             List<String> relevantIndices,
                                                                                             GraphElementPropertySchema graphElementPropertySchema,
                                                                                             Constraint constraint,
                                                                                             Class<T> tp) {
        List<Statistics.HistogramStatistics<T>> histograms = new ArrayList<>();

        String fieldType = graphElementPropertySchema.getType();
        String statTypeName = getStatTypeName(fieldType);


        Map<String, List<Statistics.BucketInfo>> fieldStatisticsPerIndex = elasticStatProvider.getFieldStatisticsPerIndex(
                this.elasticClient,
                statConfig.getStatIndexName(),
                statTypeName,
                relevantIndices,
                Collections.singletonList(graphElementSchema.getType()),
                Collections.singletonList(graphElementPropertySchema.getName())
        );

        for (Map.Entry<String, List<Statistics.BucketInfo>> entry : fieldStatisticsPerIndex.entrySet()) {
            List<Statistics.BucketInfo> sortedBuckets = Stream.ofAll(entry.getValue())
                    .sorted((o1, o2) -> o1.getLowerBound().compareTo(o2.getLowerBound())).toJavaList();

            histograms.add(new Statistics.HistogramStatistics(
                    sortedBuckets
            ));
        }

        return Statistics.HistogramStatistics.combine(histograms);
    }

    @Override
    public long getGlobalSelectivity(GraphEdgeSchema graphEdgeSchema,
                                     Rel.Direction direction,
                                     List<String> relevantIndices) {
        long globalSelectivity = 0 ;
        List<Statistics.BucketInfo> buckets = elasticStatProvider.getEdgeGlobalStatistics(
                elasticClient,
                statConfig.getStatIndexName(),
                statConfig.getStatGlobalTypeName(),
                relevantIndices,
                Collections.singletonList(graphEdgeSchema.getType()),
                Collections.singletonList(FIELD_NAME_EDGE),
                convertDirection(direction)
            );
        if (!buckets.isEmpty()) {
            Long cardinality = buckets.get(0).getCardinality();
            Long total = buckets.get(0).getTotal();
            globalSelectivity = (long) (total / (double)cardinality);
        }

        return globalSelectivity;
    }
    //endregion

    //region Private Methods
    private Statistics.SummaryStatistics getStatResultsForType(String docType, Iterable<String> indices) {
        List<Statistics.BucketInfo> buckets = elasticStatProvider.getFieldStatistics(
                elasticClient,
                statConfig.getStatIndexName(),
                statConfig.getStatTermTypeName(),
                Lists.newArrayList(indices),
                Collections.singletonList(docType),
                Collections.singletonList(FIELD_NAME_TYPE));

        long totalCount = 0;
        for (Statistics.BucketInfo bucket : buckets) {
            totalCount += bucket.getTotal();
        }
        return new Statistics.SummaryStatistics(totalCount, 1);
    }

    private long getTermBucketCount(String indexName, String docType, String term) {
        String docId = StatUtil.hashString(indexName + docType + FIELD_NAME_TYPE + term);
        return getStatBucketCount(docId);
    }

    private long getRangeBucketCount(String indexName, String docType, String field, String lowerBound, String upperBound) {
        String docId = StatUtil.hashString(indexName + docType + field + lowerBound + upperBound);
        return getStatBucketCount(docId);
    }

    private long getStatBucketCount(String docId) {
        long count = 0;
        Optional<Map<String, Object>> statDoc = elasticStatProvider.getDocumentById(
                this.elasticClient,
                statConfig.getStatIndexName(),
                statConfig.getStatTermTypeName(),
                docId);
        if (statDoc.isPresent()) {
            count = ((Number) statDoc.get().get(statConfig.getStatCountFieldName())).longValue();
        }
        return count;
    }

    private <T extends Comparable<T>> Statistics.BucketInfo getTermStatBucket(String docId) {
        Statistics.BucketInfo<T> bucketInfo = new Statistics.BucketInfo();
        Optional<Map<String, Object>> statDoc = elasticStatProvider.getDocumentById(
                this.elasticClient,
                statConfig.getStatIndexName(),
                statConfig.getStatTermTypeName(),
                docId);
        if (statDoc.isPresent()) {
            long count = ((Number) statDoc.get().get(statConfig.getStatCountFieldName())).longValue();
            long cardinality = ((Number) statDoc.get().get(statConfig.getStatCardinalityFieldName())).longValue();
            T term = (T) statDoc.get().get(FIELD_NAME_TERM);
            bucketInfo = new Statistics.BucketInfo(count, cardinality, term, term);
        }
        return bucketInfo;
    }

    private String getStatTypeName(String fieldType) {
        String statTypeName;
        switch (fieldType) {
            case STRING: { //String
                statTypeName = statConfig.getStatStringTypeName();
                break;
            }
            case INT: { //Numeric
                statTypeName = statConfig.getStatNumericTypeName();
                break;
            }
            case ENUM: { //Enum
                statTypeName = statConfig.getStatTermTypeName();
                break;
            }
            case DATE: { //Enum ? string //todo decide?
                statTypeName = statConfig.getStatTermTypeName();
                break;
            }
            default:
                throw new IllegalArgumentException("Field type is missing/invalid" + fieldType);
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
                throw new IllegalArgumentException("Not Supported Relation Direction: " + dir);
        }
    }
    //endregion

   //region Fields
    private final StatConfig statConfig;
    private final ElasticStatProvider elasticStatProvider;
    private final TransportClient elasticClient;
    //endregion

}
