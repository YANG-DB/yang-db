package com.kayhut.fuse.epb.plan.statistics.provider;

import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.kayhut.fuse.epb.plan.statistics.GraphStatisticsProvider;
import com.kayhut.fuse.epb.plan.statistics.Statistics;
import com.kayhut.fuse.epb.plan.statistics.configuration.StatConfig;
import com.kayhut.fuse.epb.plan.statistics.util.ElasticStatUtil;
import com.kayhut.fuse.epb.plan.statistics.util.StatUtil;
import com.kayhut.fuse.model.query.Constraint;
import com.kayhut.fuse.unipop.schemaProviders.GraphEdgeSchema;
import com.kayhut.fuse.unipop.schemaProviders.GraphElementPropertySchema;
import com.kayhut.fuse.unipop.schemaProviders.GraphElementSchema;
import com.kayhut.fuse.unipop.schemaProviders.GraphVertexSchema;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.TermQueryBuilder;
import org.elasticsearch.index.query.TermsQueryBuilder;

import java.util.*;

/**
 * Created by benishue on 22-May-17.
 */
public class ElasticStatisticsGraphProvider implements GraphStatisticsProvider {

    private static final String DATE = "date";
    private static final String ENUM = "enum";
    private static final String INT = "int";
    private static final String STRING = "string";

    @Inject
    public ElasticStatisticsGraphProvider(StatConfig config) {
        this.statConfig = config;
        elasticClient = new ElasticClientProvider(config).getStatClient();
    }

    @Override
    public Statistics.Cardinality getVertexCardinality(GraphVertexSchema graphVertexSchema) {
        return getVertexCardinality(graphVertexSchema,
                Lists.newArrayList(graphVertexSchema.getIndexPartition().getIndices()));
    }

    @Override
    public Statistics.Cardinality getVertexCardinality(GraphVertexSchema graphVertexSchema, List<String> relevantIndices) {
        return getStatResultsForType(graphVertexSchema.getType(), relevantIndices);
    }

    @Override
    public Statistics.Cardinality getEdgeCardinality(GraphEdgeSchema graphEdgeSchema) {
        return getEdgeCardinality(graphEdgeSchema,
                Lists.newArrayList(graphEdgeSchema.getIndexPartition().getIndices()));
    }

    @Override
    public Statistics.Cardinality getEdgeCardinality(GraphEdgeSchema graphEdgeSchema, List<String> relevantIndices) {
        return getStatResultsForType(graphEdgeSchema.getType(), relevantIndices);
    }

    @Override
    public <T extends Comparable<T>> Statistics.HistogramStatistics<T> getConditionHistogram(GraphElementSchema graphVertexSchema,
                                                                                             List<String> relevantIndices,
                                                                                             GraphElementPropertySchema graphElementPropertySchema,
                                                                                             Constraint constraint,
                                                                                             T value) {
        String statTypeName = null;
        String fieldType = graphElementPropertySchema.getType();
        switch (fieldType) {
            case STRING: { //String
                statTypeName = statConfig.getStatStringTypeName();
                break;
            }
            case INT: { //Numeric
                statTypeName = statConfig.getStatStringTypeName();
                break;
            }
            case ENUM: { //Enum
                statTypeName = statConfig.getStatTermTypeName();
                break;
            }
            case DATE: { //Enum
                statTypeName = statConfig.getStatTermTypeName();
                break;
            }
            default:
                ;//todo

        }

        List<Statistics.HistogramStatistics> histograms = new ArrayList<>();

        List<Statistics.BucketInfo> fieldStatistics = ElasticStatUtil.getFieldStatistics(
                this.elasticClient,
                statConfig.getStatIndexName(),
                statTypeName,
                relevantIndices,
                Arrays.asList(graphVertexSchema.getType()),
                Arrays.asList(graphElementPropertySchema.getName())
        );

        for(Statistics.BucketInfo bucket : fieldStatistics) {
            //Statistics.HistogramStatistics histogramStatistics = new Statistics.HistogramStatistics();

        }

//        Statistics.HistogramStatistics.combine()


        return null;
    }

    @Override
    public <T extends Comparable<T>> Statistics.HistogramStatistics<T> getConditionHistogram(GraphElementSchema graphEdgeSchema,
                                                                                             List<String> relevantIndices,
                                                                                             GraphElementPropertySchema graphElementPropertySchema,
                                                                                             Constraint constraint,
                                                                                             List<T> values) {
        return null;
    }

    @Override
    public long getGlobalSelectivity(GraphEdgeSchema graphEdgeSchema, List<String> relevantIndices) {
        return 0;
    }

    private Statistics.Cardinality getStatResultsForType(String docType, Iterable<String> indices) {
        List<Statistics.BucketInfo> buckets = ElasticStatUtil.getFieldStatistics(
                this.elasticClient,
                statConfig.getStatIndexName(),
                statConfig.getStatTermTypeName(),
                Lists.newArrayList(indices),
                Arrays.asList(docType),
                Arrays.asList("_type"));

        long totalCount = 0;
        for (Statistics.BucketInfo bucket : buckets) {
            totalCount += bucket.getTotal();
        }
        return new Statistics.Cardinality(totalCount, 1);
    }

    private <T extends Comparable<T>> Statistics.HistogramStatistics<T> getCombinedStatResultsForType(String docType, Iterable<String> indices) {
        List<Statistics.BucketInfo<T>> buckets = new ArrayList<>();
        for (String index : indices) {
            String docId = StatUtil.hashString(index + docType + "_type" + docType);
            buckets.add(getTermStatBucket(docId));
        }

        Statistics.HistogramStatistics histogram = new Statistics.HistogramStatistics<>(buckets);
        return Statistics.HistogramStatistics.<T>combine(Collections.singletonList(histogram));
    }

    private long getTermBucketCount(String indexName, String docType, String term) {
        String docId = StatUtil.hashString(indexName + docType + "_type" + term);
        return getStatBucketCount(docId);
    }

    private long getRangeBucketCount(String indexName, String docType, String field, String lowerBound, String upperBound) {
        String docId = StatUtil.hashString(indexName + docType + field + lowerBound + upperBound);
        return getStatBucketCount(docId);
    }

    private long getStatBucketCount(String docId) {
        long count = 0;
        Optional<Map<String, Object>> statDoc = ElasticStatUtil.getDocumentById(
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
        Optional<Map<String, Object>> statDoc = ElasticStatUtil.getDocumentById(
                this.elasticClient,
                statConfig.getStatIndexName(),
                statConfig.getStatTermTypeName(),
                docId);
        if (statDoc.isPresent()) {
            long count = ((Number) statDoc.get().get(statConfig.getStatCountFieldName())).longValue();
            long cardinality = ((Number) statDoc.get().get(statConfig.getStatCardinalityFieldName())).longValue();
            T term = (T) statDoc.get().get("term");
            bucketInfo = new Statistics.BucketInfo(count, cardinality, term, term);
        }
        return bucketInfo;
    }


    private SearchRequestBuilder getFieldStatisticsRequestBuilder(List<String> indices,
                                                                          String type,
                                                                          List<String> fields,
                                                                          String statIndexName,
                                                                          String statTypeName) {
        return elasticClient.prepareSearch(statIndexName)
                .setTypes(statTypeName)
                .setQuery(QueryBuilders.boolQuery()
                        .must(new TermQueryBuilder("type", type))
                        .must(new TermsQueryBuilder("field", fields))
                        .must(new TermsQueryBuilder("index", indices)))
                .setSize(Integer.MAX_VALUE);
    }

    private SearchRequestBuilder getTypeStatisticsRequestBuilder(List<String> indices,
                                                                         List<String> types,
                                                                         String statIndexName,
                                                                         String statTypeName){
        return elasticClient.prepareSearch(statIndexName)
                .setTypes(statTypeName)
                .setQuery(QueryBuilders.boolQuery()
                        .must(new TermsQueryBuilder("index", indices))
                        .must(new TermsQueryBuilder("type", types)))
                .setSize(Integer.MAX_VALUE);
    }


    public void getStatistics(SearchRequestBuilder searchQuery) {

        SearchResponse searchResponse = searchQuery.execute().actionGet();

        if(searchResponse.getHits().getTotalHits() == 0){
            ;//return getDefaultFieldStatistics();
        }

        //return getHitFieldStatistics(searchResponse.getHits());
    }

    //region Fields
    private StatConfig statConfig;
    private TransportClient elasticClient;
    //endregion

}
