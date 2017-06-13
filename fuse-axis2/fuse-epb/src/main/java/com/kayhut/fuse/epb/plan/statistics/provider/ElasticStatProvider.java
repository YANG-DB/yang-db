package com.kayhut.fuse.epb.plan.statistics.provider;

import com.kayhut.fuse.epb.plan.statistics.Statistics;
import com.kayhut.fuse.epb.plan.statistics.configuration.StatConfig;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by benishue on 24-May-17.
 */
public class ElasticStatProvider {

    private static final int RESULT_SIZE = 10000;

    private final String COUNT_FIELD_NAME;
    private final String CARDINALITY_FIELD_NAME;
    private final String STAT_FIELD_TERM_NAME;
    private final String STAT_FIELD_NUMERIC_LOWER_NAME;
    private final String STAT_FIELD_NUMERIC_UPPER_NAME;
    private final String STAT_FIELD_STRING_LOWER_NAME;
    private final String STAT_FIELD_STRING_UPPER_NAME;
    private final String STAT_INDEX_NAME;
    private final String STAT_TYPE_NUMERIC_NAME;
    private final String STAT_TYPE_STRING_NAME;
    private final String STAT_TYPE_TERM_NAME;

    public ElasticStatProvider(StatConfig conf) {
        STAT_INDEX_NAME = conf.getStatIndexName();
        COUNT_FIELD_NAME = conf.getStatCountFieldName();
        CARDINALITY_FIELD_NAME = conf.getStatCardinalityFieldName();

        STAT_TYPE_TERM_NAME = conf.getStatTermTypeName();
        STAT_TYPE_NUMERIC_NAME = conf.getStatNumericTypeName();
        STAT_TYPE_STRING_NAME = conf.getStatStringTypeName();

        STAT_FIELD_TERM_NAME = conf.getStatFieldTermName();
        STAT_FIELD_NUMERIC_LOWER_NAME = conf.getStatFieldNumericLowerName();
        STAT_FIELD_NUMERIC_UPPER_NAME = conf.getStatFieldNumericUpperName();
        STAT_FIELD_STRING_LOWER_NAME = conf.getStatFieldStringLowerName();
        STAT_FIELD_STRING_UPPER_NAME = conf.getStatFieldStringUpperName();
    }

    public Optional<Map<String, Object>> getDocumentById(TransportClient client, String indexName, String documentType, String id) {
        GetResponse r = client.get((new GetRequest(indexName, documentType, id))).actionGet();
        if (r != null && r.isExists()) {
            return Optional.ofNullable(r.getSourceAsMap());
        }
        return Optional.empty();
    }

    /**
     * @param client        Elastic client
     * @param statIndexName statistics Index Name
     * @param statTypeName  Statistics Type Name
     * @param indices       Data indices
     * @param types         Data types
     * @param fields        Data Fields
     * @return List of all buckets satisfying the input arguments
     */
    public List<Statistics.BucketInfo> getFieldStatistics(TransportClient client,
                                                          String statIndexName,
                                                          String statTypeName,
                                                          List<String> indices,
                                                          List<String> types,
                                                          List<String> fields) {

        Map<String, List<Statistics.BucketInfo>> fieldStatisticsPerIndex = getFieldStatisticsPerIndex(
                client,
                statIndexName,
                statTypeName,
                indices,
                types,
                fields);


        return fieldStatisticsPerIndex.entrySet()
                .stream()
                .map(Map.Entry::getValue)
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
    }


    /**
     * @param client        Elastic client
     * @param statIndexName statistics Index Name
     * @param statTypeName  Statistics Type Name
     * @param indices       Data indices
     * @param types         Data types
     * @param fields        Data Fields
     * @return Map<Index Name, List of buckets> of buckets group by Index name
     */
    public Map<String, List<Statistics.BucketInfo>> getFieldStatisticsPerIndex(TransportClient client,
                                                                               String statIndexName,
                                                                               String statTypeName,
                                                                               List<String> indices,
                                                                               List<String> types,
                                                                               List<String> fields) {

        Map<String, List<Statistics.BucketInfo>> bucketsPerIndex = new HashMap<>();
        SearchRequestBuilder searchRequestBuilder = getFieldsStatisticsRequestBuilder(
                client,
                statIndexName,
                statTypeName,
                indices,
                types,
                fields
        );
        SearchResponse searchResponse = searchRequestBuilder.execute().actionGet();

        if (searchResponse.getHits().getTotalHits() == 0) {
            return bucketsPerIndex;
        }

        for (SearchHit sh : searchResponse.getHits().getHits()) {
            String statType = getStatTypeValueFromHit(sh);
            String statIndex = getStatIndexValueFromHit(sh);
            long cardinality = getCardinalityValueFromHit(sh);
            long count = getCountValueFromHit(sh);
            Statistics.BucketInfo bucket = new Statistics.BucketInfo();
            if (STAT_TYPE_TERM_NAME.equals(statType)) {
                String term = getTermValueFromHit(sh);
                bucket = new Statistics.BucketInfo(
                        count,
                        cardinality,
                        term, term);
            }
            if (STAT_TYPE_NUMERIC_NAME.equals(statType)) {
                bucket = new Statistics.BucketInfo(
                        count,
                        cardinality,
                        (getLowerBoundNumericValueFromHit(sh)), getUpperBoundNumericValueFromHit(sh));
            }
            if (STAT_TYPE_STRING_NAME.equals(statType)) {
                bucket = new Statistics.BucketInfo(
                        count,
                        cardinality,
                        getLowerBoundStringValueFromHit(sh), getUpperBoundStringValueFromHit(sh));
            }

            bucketsPerIndex.computeIfAbsent(statIndex, k -> new ArrayList<>()).add(bucket);
        }
        return bucketsPerIndex;
    }


    public List<Statistics.BucketInfo> getEdgeGlobalStatistics(TransportClient client,
                                                               String statIndexName,
                                                               String statTypeName,
                                                               List<String> indices,
                                                               List<String> types,
                                                               List<String> fields,
                                                               String direction) {
        List<Statistics.BucketInfo> buckets = new ArrayList<>();
        SearchRequestBuilder searchRequestBuilder = getEdgeGlobalStatisticsRequestBuilder(
                client,
                statIndexName,
                statTypeName,
                indices,
                types,
                fields,
                direction
        );
        SearchResponse searchResponse = searchRequestBuilder.execute().actionGet();

        if (searchResponse.getHits().getTotalHits() == 0) {
            return buckets;
        }

        for (SearchHit sh : searchResponse.getHits().getHits()) {
            Statistics.BucketInfo bucket = new Statistics.BucketInfo(
                    getCountValueFromHit(sh),
                    getCardinalityValueFromHit(sh),
                    "0", "~"); // This is the global selectivity range
            buckets.add(bucket);
        }
        return buckets;
    }

    private Object getFieldValueFromHit(final SearchHit hit, final String field) {
        Map<String, Object> result = hit.sourceAsMap();
        if (result == null) {
            throw new IllegalArgumentException(String.format("%s is not found.", field));
        }
        final Object value = result.get(field);
        if (value == null) {
            throw new IllegalArgumentException(String.format("The result of %s is null.", field));
        }
        return value;
    }

    private long getCountValueFromHit(final SearchHit hit) {
        return ((Number) getFieldValueFromHit(hit, COUNT_FIELD_NAME)).longValue();
    }

    private long getCardinalityValueFromHit(final SearchHit hit) {
        return ((Number) getFieldValueFromHit(hit, CARDINALITY_FIELD_NAME)).longValue();
    }

    private String getStatTypeValueFromHit(final SearchHit hit) {
        return hit.getType();
    }

    private String getStatIndexValueFromHit(final SearchHit hit) {
        return (getFieldValueFromHit(hit, "index")).toString();
    }

    private String getTermValueFromHit(final SearchHit hit) {
        return (getFieldValueFromHit(hit, STAT_FIELD_TERM_NAME)).toString();
    }

    private String getLowerBoundStringValueFromHit(final SearchHit hit) {
        return (getFieldValueFromHit(hit, STAT_FIELD_STRING_LOWER_NAME)).toString();
    }

    private String getUpperBoundStringValueFromHit(final SearchHit hit) {
        return (getFieldValueFromHit(hit, STAT_FIELD_STRING_UPPER_NAME)).toString();
    }

    private Double getUpperBoundNumericValueFromHit(final SearchHit hit) {
        return ((Number) getFieldValueFromHit(hit, STAT_FIELD_NUMERIC_UPPER_NAME)).doubleValue();
    }

    private Double getLowerBoundNumericValueFromHit(final SearchHit hit) {
        return ((Number) getFieldValueFromHit(hit, STAT_FIELD_NUMERIC_LOWER_NAME)).doubleValue();
    }

    private SearchRequestBuilder getFieldsStatisticsRequestBuilder(TransportClient client,
                                                                   String statIndexName,
                                                                   String statTypeName,
                                                                   List<String> indices,
                                                                   List<String> types,
                                                                   List<String> fields) {

        return client.prepareSearch(statIndexName)
                .setTypes(statTypeName)
                .setQuery(QueryBuilders.boolQuery()
                        .filter(QueryBuilders.boolQuery()
                                .must(QueryBuilders.termsQuery("index", indices))
                                .must(QueryBuilders.termsQuery("type", types))
                                .must(QueryBuilders.termsQuery("field", fields))

                        ))
                .setSize(RESULT_SIZE);
    }


    private SearchRequestBuilder getEdgeGlobalStatisticsRequestBuilder(TransportClient client,
                                                                       String statIndexName,
                                                                       String statTypeName,
                                                                       List<String> indices,
                                                                       List<String> types,
                                                                       List<String> fields,
                                                                       String direction) {
        return client.prepareSearch(statIndexName)
                .setTypes(statTypeName)
                .setQuery(QueryBuilders.boolQuery()
                        .filter(QueryBuilders.boolQuery()
                                .must(QueryBuilders.termsQuery("index", indices))
                                .must(QueryBuilders.termsQuery("type", types))
                                .must(QueryBuilders.termsQuery("field", fields))
                                .must(QueryBuilders.termQuery("direction", direction))

                        ))
                .setSize(RESULT_SIZE);
    }

}
