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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

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

    public ElasticStatProvider(StatConfig statConfig) {
        STAT_INDEX_NAME = statConfig.getStatIndexName();
        COUNT_FIELD_NAME = statConfig.getStatCountFieldName();
        CARDINALITY_FIELD_NAME = statConfig.getStatCardinalityFieldName();

        STAT_TYPE_TERM_NAME = statConfig.getStatTermTypeName();
        STAT_TYPE_NUMERIC_NAME = statConfig.getStatNumericTypeName();
        STAT_TYPE_STRING_NAME = statConfig.getStatStringTypeName();

        STAT_FIELD_TERM_NAME = statConfig.getStatFieldTermName();
        STAT_FIELD_NUMERIC_LOWER_NAME = statConfig.getStatFieldNumericLowerName();
        STAT_FIELD_NUMERIC_UPPER_NAME = statConfig.getStatFieldNumericUpperName();
        STAT_FIELD_STRING_LOWER_NAME = statConfig.getStatFieldStringLowerName();
        STAT_FIELD_STRING_UPPER_NAME = statConfig.getStatFieldStringUpperName();
    }

    public Optional<Map<String, Object>> getDocumentById(TransportClient client, String indexName, String documentType, String id) {
        GetResponse r = client.get((new GetRequest(indexName, documentType, id))).actionGet();
        if (r != null && r.isExists()) {
            return Optional.ofNullable(r.getSourceAsMap());
        }
        return Optional.empty();
    }

    public List<Statistics.BucketInfo> getFieldStatistics(TransportClient client,
                                                          String statIndexName,
                                                          String statTypeName,
                                                          List<String> indices,
                                                          List<String> types,
                                                          List<String> fields) {
        List<Statistics.BucketInfo> buckets = new ArrayList<>();
        SearchRequestBuilder searchRequestBuilder = getFieldsStatisticsElasticRequestBuilder(
                client,
                statIndexName,
                statTypeName,
                indices,
                types,
                fields
        );
        SearchResponse searchResponse = searchRequestBuilder.execute().actionGet();

        if (searchResponse.getHits().getTotalHits() == 0) {
            return buckets;
        }

        for (SearchHit sh : searchResponse.getHits().getHits()) {
            String statType = getStatTypeValueFromHit(sh);
            if (STAT_TYPE_TERM_NAME.equals(statType)) {
                String term = getTermValueFromHit(sh);
                buckets.add(new Statistics.BucketInfo(
                        getCountValueFromHit(sh),
                        getCardinalityValueFromHit(sh),
                        term, term));
            }
            if (STAT_TYPE_NUMERIC_NAME.equals(statType)) {
                buckets.add(new Statistics.BucketInfo(
                        getCountValueFromHit(sh),
                        getCardinalityValueFromHit(sh),
                        (getLowerBoundNumericValueFromHit(sh)), getUpperBoundNumericValueFromHit(sh)));
            }
            if (STAT_TYPE_STRING_NAME.equals(statType)) {
                buckets.add(new Statistics.BucketInfo(
                        getCountValueFromHit(sh),
                        getCardinalityValueFromHit(sh),
                        getLowerBoundStringValueFromHit(sh), getUpperBoundStringValueFromHit(sh)));
            }
        }
        return buckets;
    }


    public Map<String, List<Statistics.BucketInfo>> getFieldStatisticsPerIndex(TransportClient client,
                                                                               String statIndexName,
                                                                               String statTypeName,
                                                                               List<String> indices,
                                                                               List<String> types,
                                                                               List<String> fields) {

        return null;
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
        return ((Double) getFieldValueFromHit(hit, STAT_FIELD_NUMERIC_UPPER_NAME));
    }

    private Double getLowerBoundNumericValueFromHit(final SearchHit hit) {
        return ((Double) getFieldValueFromHit(hit, STAT_FIELD_NUMERIC_LOWER_NAME));
    }

    private SearchRequestBuilder getFieldsStatisticsElasticRequestBuilder(TransportClient client,
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

}
