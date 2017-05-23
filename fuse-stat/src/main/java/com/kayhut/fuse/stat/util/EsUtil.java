package com.kayhut.fuse.stat.util;

import com.kayhut.fuse.stat.model.enums.DataType;
import com.kayhut.fuse.stat.model.bucket.BucketRange;
import com.kayhut.fuse.stat.model.result.StatResult;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.LineIterator;
import org.elasticsearch.action.admin.cluster.state.ClusterStateResponse;
import org.elasticsearch.action.bulk.BackoffPolicy;
import org.elasticsearch.action.bulk.BulkProcessor;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.Requests;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.cluster.ClusterState;
import org.elasticsearch.cluster.metadata.IndexMetaData;
import org.elasticsearch.cluster.metadata.MappingMetaData;
import org.elasticsearch.common.collect.ImmutableOpenMap;
import org.elasticsearch.common.unit.ByteSizeUnit;
import org.elasticsearch.common.unit.ByteSizeValue;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.filters.Filters;
import org.elasticsearch.search.aggregations.bucket.filters.FiltersAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.range.Range;
import org.elasticsearch.search.aggregations.bucket.range.RangeBuilder;
import org.elasticsearch.search.aggregations.metrics.cardinality.InternalCardinality;

import java.io.File;
import java.io.IOException;
import java.util.*;


/**
 * Created by benishue on 01-May-17.
 */
public class EsUtil {

    private static final String AGG_EXTENDED_STATS = "extended_stats";
    private static final String AGG_CARDINALITY = "cardinality";


    private EsUtil() {
        throw new IllegalAccessError("Utility class");
    }

    public static List<StatResult> getNumericHistogramResults(TransportClient client,
                                                              String indexName,
                                                              String typeName,
                                                              String fieldName,
                                                              double min, double max,
                                                              long numOfBins) {

        List<BucketRange<Double>> buckets = StatUtil.createNumericBuckets(min, max, Math.toIntExact(numOfBins));

        return getNumericBucketsStatResults(client, indexName, typeName, fieldName, buckets);
    }

    public static List<StatResult> getManualHistogramResults(TransportClient client,
                                                             String indexName,
                                                             String typeName,
                                                             String fieldName,
                                                             DataType dataType,
                                                             List<BucketRange<? extends Object>> buckets) {

        List<StatResult> bucketStatResults = new ArrayList<>();

        if (DataType.string == dataType) {
            List<BucketRange<String>> stringBucketRanges = new ArrayList<>();
            buckets.forEach(bucketRange -> stringBucketRanges.add(new BucketRange<>((String) bucketRange.getStart(), (String) bucketRange.getEnd())));
            bucketStatResults = getStringBucketsStatResults(client, indexName, typeName, fieldName, stringBucketRanges);
        }

        if (DataType.numeric == dataType) {
            List<BucketRange<Double>> numericBucketRanges = new ArrayList<>();
            buckets.forEach(bucketRange -> numericBucketRanges.add(new BucketRange<>((Double) bucketRange.getStart(), (Double) bucketRange.getEnd())));
            bucketStatResults = getNumericBucketsStatResults(client, indexName, typeName, fieldName, numericBucketRanges);
        }
        return bucketStatResults;
    }

    private static List<StatResult> getNumericBucketsStatResults(Client client,
                                                                 String indexName,
                                                                 String typeName,
                                                                 String fieldName,
                                                                 List<BucketRange<Double>> buckets) {
        List<StatResult> bucketStatResults = new ArrayList<>();
        String aggName = buildAggName(indexName, typeName, fieldName);

        SearchRequestBuilder searchRequestBuilder = client.prepareSearch(indexName)
                .setTypes(typeName);

        RangeBuilder rangesAggregationBuilder = AggregationBuilders.range(aggName).field(fieldName);
        buckets.forEach(bucket -> {
            Double start = bucket.getStart();
            Double end = bucket.getEnd();
            rangesAggregationBuilder.addRange(start, end);
        });

        SearchResponse sr = searchRequestBuilder.addAggregation(rangesAggregationBuilder
                .subAggregation(AggregationBuilders.cardinality(AGG_CARDINALITY).field(fieldName)))
                .execute().actionGet();

        Range agg = sr.getAggregations().get(aggName);

        for (Range.Bucket entry : agg.getBuckets()) {
            String key = entry.getKeyAsString();             // Range as key
            Number from = (Number) entry.getFrom();          // Bucket from
            Number to = (Number) entry.getTo();              // Bucket to
            long docCount = entry.getDocCount();    // Doc count

            InternalCardinality cardinality = entry.getAggregations().get(AGG_CARDINALITY);
            StatResult<Double> bucketStatResult = new StatResult(indexName, typeName, fieldName,
                    key,
                    DataType.numeric,
                    from,
                    to,
                    docCount,
                    cardinality.getValue());

            bucketStatResults.add(bucketStatResult);
        }

        return bucketStatResults;
    }

    public static List<StatResult> getStringBucketsStatResults(TransportClient client,
                                                               String indexName,
                                                               String typeName,
                                                               String fieldName,
                                                               List<BucketRange<String>> buckets) {

        List<StatResult> bucketStatResults = new ArrayList<>();

        String aggName = buildAggName(indexName, typeName, fieldName);
        SearchRequestBuilder searchRequestBuilder = client.prepareSearch(indexName)
                .setTypes(typeName);


        FiltersAggregationBuilder filtersAggregationBuilder = AggregationBuilders.filters(aggName);
        buckets.forEach(bucket -> {
            String bucketKey = bucket.getStart() + "_" + bucket.getEnd();
            filtersAggregationBuilder.filter(bucketKey, QueryBuilders.rangeQuery(fieldName).from(bucket.getStart()).to(bucket.getEnd()));
        });

        SearchResponse sr = searchRequestBuilder.addAggregation(filtersAggregationBuilder
                .subAggregation(AggregationBuilders.cardinality(AGG_CARDINALITY).field(fieldName)))
                .execute().actionGet();

        Filters aggregation = sr.getAggregations().get(aggName);

        for (Filters.Bucket entry : aggregation.getBuckets()) {
            String key = entry.getKeyAsString();            // bucket key
            long docCount = entry.getDocCount();            // Doc count
            String start = key.split("_")[0];          // Bucket start
            String end = key.split("_")[1];              // Bucket end
            InternalCardinality cardinality = entry.getAggregations().get(AGG_CARDINALITY);

            StatResult<String> bucketStatResult = new StatResult(indexName, typeName, fieldName,
                    key,
                    DataType.string,
                    start,
                    end,
                    docCount,
                    cardinality.getValue());

            bucketStatResults.add(bucketStatResult);
        }
        return bucketStatResults;
    }

    private static String buildAggName(String indexName, String typeName, String fieldName) {
        return indexName + "_" + typeName + "_" + fieldName + "_" + "hist";
    }

    public static void bulkIndexingFromFile(TransportClient client, String filePath, String index, String type) throws IOException {
        BulkProcessor bulkProcessor = BulkProcessor.builder(
                client,
                new BulkProcessor.Listener() {
                    @Override
                    public void beforeBulk(long executionId,
                                           BulkRequest request) {
                        // Do nothing
                    }

                    @Override
                    public void afterBulk(long executionId,
                                          BulkRequest request,
                                          BulkResponse response) {
                        // Do nothing
                    }

                    @Override
                    public void afterBulk(long executionId,
                                          BulkRequest request,
                                          Throwable failure) {
                        // Do nothing
                    }
                })
                .setBulkActions(100)
                .setBulkSize(new ByteSizeValue(1, ByteSizeUnit.MB))
                .setFlushInterval(TimeValue.timeValueSeconds(5))
                .setConcurrentRequests(1)
                .setBackoffPolicy(
                        BackoffPolicy.exponentialBackoff(TimeValue.timeValueMillis(100), 3))
                .build();

        File file = FileUtils.getFile(filePath);

        LineIterator it = FileUtils.lineIterator(file, "UTF-8");
        try {
            int i = 1;
            while (it.hasNext()) {
                String line = it.nextLine();
                bulkProcessor.add(new IndexRequest(index, type, String.valueOf(i))
                        .source(line));
                i++;
            }
        } finally {
            it.close();
        }

        bulkProcessor.close();


    }

    public static void showTypeFieldsNames(TransportClient esClient, String indexName, String typeName) throws IOException {

        ClusterState cs = esClient.admin().cluster().prepareState().setIndices(indexName).execute().actionGet().getState();
        IndexMetaData imd = cs.getMetaData().index(indexName);
        MappingMetaData mdd = imd.mapping(typeName);
        Map<String, Object> map = null;
        map = mdd.getSourceAsMap();
        List<String> fieldList = getList("", map);
        System.out.println("Field List:");
        for (String field : fieldList) {
            System.out.println(field);
        }
    }

    private static List<String> getList(String fieldName, Map<String, Object> mapProperties) {
        List<String> fieldList = new ArrayList<>();
        Map<String, Object> map = (Map<String, Object>) mapProperties.get("properties");
        Set<String> keys = map.keySet();
        for (String key : keys) {
            if (((Map<String, Object>) map.get(key)).containsKey("type")) {
                fieldList.add(fieldName + "" + key);
            } else {
                List<String> tempList = getList(fieldName + "" + key + ".", (Map<String, Object>) map.get(key));
                fieldList.addAll(tempList);
            }
        }
        return fieldList;
    }

    public static boolean checkIfEsIndexExists(Client client, String index) {

        IndexMetaData indexMetaData = client.admin().cluster()
                .state(Requests.clusterStateRequest())
                .actionGet()
                .getState()
                .getMetaData()
                .index(index);

        return (indexMetaData != null);

    }

    public static boolean checkIfEsTypeExists(Client client, String index, String type) {
        ClusterStateResponse resp =
                client.admin().cluster().prepareState().execute().actionGet();
        ImmutableOpenMap<String, MappingMetaData> mappings = resp.getState().metaData().index(index).getMappings();
        return mappings.containsKey(type);
    }

    public static boolean checkIfEsDocExists(Client client, String index, String type, String docId) {
        // Check if a document exists
        GetResponse response = client.prepareGet(index, type, docId).setRefresh(true).execute().actionGet();
        return response.isExists();
    }

    public static Optional<Map<String, Object>> getDocumentById(Client client, String indexName, String documentType, String id) {
        GetResponse r = client.get((new GetRequest(indexName, documentType, id))).actionGet();
        if (r != null && r.isExists()) {
            return Optional.ofNullable(r.getSourceAsMap());
        }
        return Optional.empty();
    }
}
