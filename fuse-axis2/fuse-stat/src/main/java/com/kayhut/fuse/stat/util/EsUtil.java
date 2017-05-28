package com.kayhut.fuse.stat.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kayhut.fuse.stat.model.bucket.BucketRange;
import com.kayhut.fuse.stat.model.bucket.BucketTerm;
import com.kayhut.fuse.stat.model.enums.DataType;
import com.kayhut.fuse.stat.model.result.StatRangeResult;
import com.kayhut.fuse.stat.model.result.StatTermResult;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.LineIterator;
import org.elasticsearch.action.admin.cluster.state.ClusterStateResponse;
import org.elasticsearch.action.admin.indices.stats.IndicesStatsRequest;
import org.elasticsearch.action.bulk.BackoffPolicy;
import org.elasticsearch.action.bulk.BulkProcessor;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.Requests;
import org.elasticsearch.client.transport.TransportClient;
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

    /**
     * @param client Elastic client
     * @param indexName Elastic index name (e.g., index1)
     * @param typeName Elastic type name (e.g., Dragon)
     * @param fieldName Elastic field name
     * @param min The lower bound of the left most range bucket
     * @param max the upper bound of the right most range bucket
     * @param numOfBins number of buckets
     * @return List of numeric range buckets with lower (inclusive) and upper bound (exclusive)
     */
    public static List<StatRangeResult> getNumericHistogramResults(TransportClient client,
                                                                   String indexName,
                                                                   String typeName,
                                                                   String fieldName,
                                                                   double min, double max,
                                                                   long numOfBins) {

        List<BucketRange<Double>> buckets = StatUtil.createNumericBuckets(min, max, Math.toIntExact(numOfBins));

        return getNumericBucketsStatResults(client, indexName, typeName, fieldName, buckets);
    }

    public static <T> List<StatRangeResult> getManualHistogramResults(TransportClient client,
                                                                  String indexName,
                                                                  String typeName,
                                                                  String fieldName,
                                                                  DataType dataType,
                                                                  List<BucketRange<T>> buckets) {

        List<StatRangeResult> bucketStatResults = new ArrayList<>();

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

    private static List<StatRangeResult> getNumericBucketsStatResults(Client client,
                                                                      String indexName,
                                                                      String typeName,
                                                                      String fieldName,
                                                                      List<BucketRange<Double>> buckets) {
        List<StatRangeResult> bucketStatResults = new ArrayList<>();
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
                .setSize(0).execute().actionGet();

        Range agg = sr.getAggregations().get(aggName);

        for (Range.Bucket entry : agg.getBuckets()) {
            String key = entry.getKeyAsString();             // Range as key
            Number from = (Number) entry.getFrom();          // Bucket from
            Number to = (Number) entry.getTo();              // Bucket to
            long docCount = entry.getDocCount();    // Doc count

            InternalCardinality cardinality = entry.getAggregations().get(AGG_CARDINALITY);
            StatRangeResult bucketStatResult = new StatRangeResult(indexName, typeName, fieldName,
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

    /**
     * @param client Elastic client
     * @param indexName Elastic index name (e.g., index1)
     * @param typeName Elastic type name (e.g., Dragon)
     * @param fieldName Elastic field name (e.g., Address)
     * @param buckets - String buckets ["str1", "str2")
     * @return List of String range buckets with lower (inclusive) and upper bound (exclusive)
     */
    public static List<StatRangeResult> getStringBucketsStatResults(TransportClient client,
                                                                    String indexName,
                                                                    String typeName,
                                                                    String fieldName,
                                                                    List<BucketRange<String>> buckets) {

        List<StatRangeResult> bucketStatResults = new ArrayList<>();

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
                .setSize(0).execute().actionGet();

        Filters aggregation = sr.getAggregations().get(aggName);

        for (Filters.Bucket entry : aggregation.getBuckets()) {
            String key = entry.getKeyAsString();            // bucket key
            long docCount = entry.getDocCount();            // Doc count
            String start = key.split("_")[0];          // Bucket start
            String end = key.split("_")[1];              // Bucket end
            InternalCardinality cardinality = entry.getAggregations().get(AGG_CARDINALITY);

            StatRangeResult bucketStatResult = new StatRangeResult(indexName, typeName, fieldName,
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

    public static List<StatTermResult> getTermHistogramResults(TransportClient client,
                                                               String indexName,
                                                               String typeName,
                                                               String fieldName,
                                                               DataType dataType,
                                                               List<BucketTerm> buckets) {

        List<StatTermResult> bucketStatResults = new ArrayList<>();

        String aggName = buildAggName(indexName, typeName, fieldName);
        SearchRequestBuilder searchRequestBuilder = client.prepareSearch(indexName)
                .setTypes(typeName);

        FiltersAggregationBuilder filtersAggregationBuilder = AggregationBuilders.filters(aggName);
        buckets.forEach(bucket -> filtersAggregationBuilder.filter((String) bucket.getTerm(), QueryBuilders.termQuery(fieldName, bucket.getTerm())));

        SearchResponse sr = searchRequestBuilder.addAggregation(filtersAggregationBuilder
                .subAggregation(AggregationBuilders.cardinality(AGG_CARDINALITY).field(fieldName)))
                .setSize(0).execute().actionGet();

        Filters aggregation = sr.getAggregations().get(aggName);

        for (Filters.Bucket entry : aggregation.getBuckets()) {
            String key = entry.getKeyAsString();            // bucket key
            long docCount = entry.getDocCount();            // Doc count
            InternalCardinality cardinality = entry.getAggregations().get(AGG_CARDINALITY);

            StatTermResult bucketStatResult = new StatTermResult(indexName, typeName, fieldName,
                    key,
                    dataType,
                    key,
                    docCount,
                    cardinality.getValue());

            bucketStatResults.add(bucketStatResult);
        }
        return bucketStatResults;
    }

    private static String buildAggName(String indexName, String typeName, String fieldName) {
        return String.format("%s_%s_%s_hist", indexName, typeName, fieldName);
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

    /**
     * @param client
     * @param indexName
     * @param documentType
     * @param id
     * @return Elastic source document (As a map)
     */
    public static Optional<Map<String, Object>> getDocumentSourceById(Client client, String indexName, String documentType, String id) {
        GetResponse r = getGetResponse(client, indexName, documentType, id);
        if (r != null && r.isExists()) {
            return Optional.ofNullable(r.getSourceAsMap());
        }
        return Optional.empty();
    }

    private static GetResponse getGetResponse(Client client, String indexName, String documentType, String id) {
        return client.get((new GetRequest(indexName, documentType, id))).actionGet();
    }

    public static Optional<String> getDocumentTypeByDocId(Client client, String indexName, String documentType, String docId) {
        GetResponse r = getGetResponse(client, indexName, documentType, docId);
        if (r != null && r.isExists()) {
            return Optional.ofNullable(r.getType());
        }
        return Optional.empty();
    }

    public static SearchResponse getAllDocuments(Client client, String index,
                                                 String type) {
        return client.prepareSearch(index).setTypes(type).execute()
                .actionGet();
    }

    /**
     * Return all the documents from a cluster.
     * @param client
     * @return
     */
    public static SearchResponse getAllDocuments(Client client) {
        return client.prepareSearch().execute().actionGet();
    }

    /**
     * Return all indices from cluster.
     * @param client
     * @return array of Indices
     */
    public static String[] getAllIndices(Client client) {
        Set<String> indicesSet = client.admin().indices().stats(new IndicesStatsRequest())
                .actionGet().getIndices().keySet();
        return indicesSet.toArray(new String[indicesSet.size()]);
    }

    /**
     * Return all mappings of given index
     * @param client Elastic Client
     * @param index Index Name
     * @return
     */
    public static ImmutableOpenMap<String, MappingMetaData> getMappingsOfIndex(
            Client client, String index) {
        ClusterStateResponse clusterStateResponse = client.admin().cluster()
                .prepareState().execute().actionGet();
        return clusterStateResponse.getState().getMetaData().index(index)
                .getMappings();
    }

    /**
     * Get all types in given index
     * @param client Elastic Client
     * @param index Index Name
     * @return array of Elastic types
     */
    public static String[] getAllTypesFromIndex(
            Client client, String index) {
        return getMappingsOfIndex(client, index).keys().toArray(String.class);
    }

    /**
     * Index given document.
     * @param client: Client used to index data
     * @param index: Document is stored in this index
     * @param type: Document stored in this type
     * @param id: Specifies _id of the document
     * @param document: Represents body of the document
     * @return {@link IndexResponse}
     */
    public static IndexResponse indexData(Client client, String index,
                                          String type, String id, String document) {
        return client.prepareIndex(index, type, id)
                .setSource(document).execute().actionGet();
    }

    /**
     * Index given object
     * @param client: Client used to index data
     * @param index: Document is stored in this index
     * @param type: Document stored in this type
     * @param id : Specifies id of the document
     * @param obj: Object to index
     * @return {@link IndexResponse}
     */
    public static IndexResponse indexData(Client client, String index,
                                          String type, String id, Object obj) throws JsonProcessingException {
        return indexData(client, index, type, id, new ObjectMapper().writeValueAsString(obj));
    }
}
