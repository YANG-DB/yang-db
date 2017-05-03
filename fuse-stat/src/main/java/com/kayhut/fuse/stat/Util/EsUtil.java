package com.kayhut.fuse.stat.Util;

import com.kayhut.fuse.stat.model.configuration.Bucket;
import com.kayhut.fuse.stat.model.result.BucketStatResult;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.LineIterator;
import org.elasticsearch.action.bulk.BackoffPolicy;
import org.elasticsearch.action.bulk.BulkProcessor;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.cluster.ClusterState;
import org.elasticsearch.cluster.metadata.IndexMetaData;
import org.elasticsearch.cluster.metadata.MappingMetaData;
import org.elasticsearch.common.unit.ByteSizeUnit;
import org.elasticsearch.common.unit.ByteSizeValue;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.filters.Filters;
import org.elasticsearch.search.aggregations.bucket.filters.FiltersAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.histogram.Histogram;
import org.elasticsearch.search.aggregations.bucket.range.Range;
import org.elasticsearch.search.aggregations.bucket.range.RangeBuilder;
import org.elasticsearch.search.aggregations.metrics.cardinality.InternalCardinality;
import org.elasticsearch.search.aggregations.metrics.stats.extended.InternalExtendedStats;

import java.io.File;
import java.io.IOException;
import java.util.*;


/**
 * Created by benishue on 01-May-17.
 */
public class EsUtil {

    public final static String AGG_TERMS = "terms";
    public final static String AGG_STATS = "stats";
    public final static String AGG_TERMS_STATS = "termsstats";
    public static final String AGG_FILTER = "filter";
    public static final String AGG_EXTENDED_STATS = "extended_stats";
    public static final String AGG_CARDINALITY = "cardinality";

    public static List<BucketStatResult> getNumericHistogramResults(TransportClient client,
                                                                    String indexName,
                                                                    String typeName,
                                                                    String fieldName,
                                                                    long min, long max,
                                                                    long interval){

        List<BucketStatResult> bucketStatResults = new ArrayList<>();

        String aggName = buildAggName(indexName,typeName,fieldName);
        SearchResponse sr =  client.prepareSearch(indexName)
                .setTypes(typeName)
                .addAggregation(AggregationBuilders.histogram(aggName)
                        .field(fieldName)
                        .interval(interval)
                        .minDocCount(0)
                        .extendedBounds(min, max)
                        .subAggregation(AggregationBuilders.extendedStats(AGG_EXTENDED_STATS).field(fieldName))
                        .subAggregation(AggregationBuilders.cardinality(AGG_CARDINALITY).field(fieldName))
                )
                .execute().actionGet();


        Histogram histogram = sr.getAggregations().get(aggName);


        // For each entry
        for (Histogram.Bucket entry : histogram.getBuckets()) {
            Number key = (Number) entry.getKey();   // Key
            long docCount = entry.getDocCount();    // Doc count
            InternalExtendedStats extendedStats = entry.getAggregations().get(AGG_EXTENDED_STATS);
            InternalCardinality cardinality = entry.getAggregations().get(AGG_CARDINALITY);
            BucketStatResult bucketStatResult = new BucketStatResult(indexName, typeName, fieldName,
                    key.toString(),
                    Long.toString(min),
                    Long.toString(max),
                    extendedStats.getCount(),
                    extendedStats.getSum(),
                    extendedStats.getSumOfSquares(),
                    extendedStats.getAvg(),
                    extendedStats.getMin(),
                    extendedStats.getMax(),
                    extendedStats.getVariance(),
                    extendedStats.getStdDeviation(),
                    cardinality.getValue());

            bucketStatResults.add(bucketStatResult);
        }

        return bucketStatResults;
    }

    public static List<BucketStatResult> getManualHistogramResults(TransportClient client,
                                                                   String indexName,
                                                                   String typeName,
                                                                   String fieldName,
                                                                   String dataType,
                                                                   List<Bucket> buckets){

        List<BucketStatResult> bucketStatResults = new ArrayList<>();

        String aggName = buildAggName(indexName,typeName,fieldName);
        SearchRequestBuilder searchRequestBuilder = client.prepareSearch(indexName)
                .setTypes(typeName);

        if (dataType.equals("string")) {
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

                BucketStatResult bucketStatResult = new BucketStatResult(indexName, typeName, fieldName,
                        key,
                        start,
                        end,
                        docCount,
                        cardinality.getValue());

                bucketStatResults.add(bucketStatResult);
            }
        }

        if (dataType.equals("numeric")) {
            RangeBuilder rangesAggregationBuilder = AggregationBuilders.range(aggName).field(fieldName);
            buckets.forEach(bucket -> {
                Long start = Long.parseLong(bucket.getStart());
                Long end = Long.parseLong(bucket.getEnd());
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
                BucketStatResult bucketStatResult = new BucketStatResult(indexName, typeName, fieldName,
                        key,
                        from.toString(),
                        to.toString(),
                        docCount,
                        cardinality.getValue());

                bucketStatResults.add(bucketStatResult);
            }
        }
        return bucketStatResults;
    }

    private static String buildAggName(String indexName, String typeName, String fieldName){
        return indexName + "_" + typeName + "_" + fieldName + "_" + "hist";
    }

    public static void bulkIndexingFromFile(TransportClient client, String filePath, String index, String type) throws IOException {
        BulkProcessor bulkProcessor = BulkProcessor.builder(
                client,
                new BulkProcessor.Listener() {
                    @Override
                    public void beforeBulk(long executionId,
                                           BulkRequest request) {  }

                    @Override
                    public void afterBulk(long executionId,
                                          BulkRequest request,
                                          BulkResponse response) {  }

                    @Override
                    public void afterBulk(long executionId,
                                          BulkRequest request,
                                          Throwable failure) {  }
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
                bulkProcessor.add((IndexRequest) new IndexRequest(index, type, String.valueOf(i))
                        .source(line));
                i++;
            }
        } finally {
            it.close();
        }

        bulkProcessor.close();


    }

    public static void showTypeFieldsNames(TransportClient esClient, String indexName, String typeName) {

        List<String> fieldList = new ArrayList<String>();
        ClusterState cs = esClient.admin().cluster().prepareState().setIndices(indexName).execute().actionGet().getState();
        IndexMetaData imd = cs.getMetaData().index(indexName);
        MappingMetaData mdd = imd.mapping(typeName);
        Map<String, Object> map = null;
        try {
            map = mdd.getSourceAsMap();
        } catch (IOException e) {
            e.printStackTrace();
        }
        fieldList = getList("", map);
        System.out.println("Field List:");
        for (String field : fieldList) {
            System.out.println(field);
        }
    }

    private static List<String> getList(String fieldName, Map<String, Object> mapProperties) {
        List<String> fieldList = new ArrayList<String>();
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
}
