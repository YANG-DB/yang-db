package com.kayhut.fuse.epb.plan.statistics.provider;

import com.google.inject.Inject;
import com.kayhut.fuse.epb.plan.statistics.Statistics;
import com.kayhut.fuse.epb.plan.statistics.configuration.StatConfig;
import com.kayhut.fuse.unipop.controller.utils.map.MapBuilder;
import javaslang.collection.Stream;
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
    //region Static
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
    //endregion

    //region Constructors
    @Inject
    public ElasticStatProvider(StatConfig conf, StatDataProvider statDataProvider) {
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

        this.statDataProvider = statDataProvider;
    }
    //endregion

    //region Public Methods
    /**
     * @param indices       Data indices
     * @param types         Data types
     * @param fields        Data Fields
     * @return List of all buckets satisfying the input arguments
     */
    public List<Statistics.BucketInfo> getFieldStatistics(
            List<String> indices,
            List<String> types,
            List<String> fields) {
        return Stream.ofAll(getFieldStatisticsPerIndex(indices, types, fields).values())
                .flatMap(Stream::ofAll)
                .toJavaList();
    }


    /**
     * @param indices       Data indices
     * @param types         Data types
     * @param fields        Data Fields
     * @return Map<Index Name, List of buckets> of buckets group by Index name
     */
    public Map<String, List<Statistics.BucketInfo>> getFieldStatisticsPerIndex(
            List<String> indices,
            List<String> types,
            List<String> fields) {

        Iterable<Map<String, Object>> statDocuments = this.statDataProvider.getStatDataItems(indices, types, fields, Collections.emptyMap());
        Map<String, List<Statistics.BucketInfo>> bucketsPerIndex = new HashMap<>();

        for (Map<String, Object> statDocument : statDocuments) {
            String statIndex = statDocument.get("index").toString();
            String statType = statDocument.get("_type").toString();
            long cardinality = ((Number)statDocument.get(CARDINALITY_FIELD_NAME)).longValue();
            long count = ((Number)statDocument.get(COUNT_FIELD_NAME)).longValue();

            Statistics.BucketInfo bucket = null;
            if (STAT_TYPE_TERM_NAME.equals(statType)) {
                String term = statDocument.get(STAT_FIELD_TERM_NAME).toString();
                bucket = new Statistics.BucketInfo<>(
                        count,
                        cardinality,
                        term, term);
            }
            if (STAT_TYPE_NUMERIC_NAME.equals(statType)) {
                bucket = new Statistics.BucketInfo(
                        count,
                        cardinality,
                        ((Number)statDocument.get(STAT_FIELD_NUMERIC_LOWER_NAME)).doubleValue(),
                        ((Number)statDocument.get(STAT_FIELD_NUMERIC_UPPER_NAME)).doubleValue());
            }
            if (STAT_TYPE_STRING_NAME.equals(statType)) {
                bucket = new Statistics.BucketInfo<>(
                        count,
                        cardinality,
                        statDocument.get(STAT_FIELD_STRING_LOWER_NAME).toString(),
                        statDocument.get(STAT_FIELD_STRING_UPPER_NAME).toString());
            }

            bucketsPerIndex.computeIfAbsent(statIndex, k -> new ArrayList<>()).add(bucket);
        }

        return bucketsPerIndex;
    }

    public List<Statistics.BucketInfo<String>> getEdgeGlobalStatistics(
            List<String> indices,
            List<String> types,
            List<String> fields,
            String direction) {
        Iterable<Map<String, Object>> statDocuments = this.statDataProvider.getStatDataItems(
                indices,
                types,
                fields,
                new MapBuilder<String, Object>().put("direction", direction).get());

        return Stream.ofAll(statDocuments)
                .map(statDocument -> {
                    return new Statistics.BucketInfo<>(
                            ((Number)statDocument.get(COUNT_FIELD_NAME)).longValue(),
                            ((Number)statDocument.get(CARDINALITY_FIELD_NAME)).longValue(),
                            "0", "~"); // This is the global selectivity range
                }).toJavaList();
    }
    //endregion

    //region Private Methods
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
    //endregion

    //region Fields
    private StatDataProvider statDataProvider;
    //endregion
}
