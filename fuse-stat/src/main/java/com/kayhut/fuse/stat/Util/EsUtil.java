package com.kayhut.fuse.stat.Util;

import com.kayhut.fuse.stat.model.result.BucketStatResult;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.LineIterator;
import org.elasticsearch.action.bulk.BackoffPolicy;
import org.elasticsearch.action.bulk.BulkProcessor;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.cluster.ClusterState;
import org.elasticsearch.cluster.metadata.IndexMetaData;
import org.elasticsearch.cluster.metadata.MappingMetaData;
import org.elasticsearch.common.unit.ByteSizeUnit;
import org.elasticsearch.common.unit.ByteSizeValue;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.histogram.Histogram;
import org.elasticsearch.search.aggregations.metrics.cardinality.InternalCardinality;
import org.elasticsearch.search.aggregations.metrics.stats.extended.InternalExtendedStats;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;


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

    public static List<BucketStatResult> getNumericHistogram(TransportClient client,
                                           String indexName,
                                           String typeName,
                                           String fieldName,
                                           long min, long max,
                                           long interval){

        List<BucketStatResult> bucketStatResults = new ArrayList<>();

        String histogramName = buildHistogramName(indexName,typeName,fieldName);
        SearchResponse sr =  client.prepareSearch(indexName)
                .setTypes(typeName)
                .addAggregation(AggregationBuilders.histogram(histogramName)
                        .field(fieldName)
                        .interval(interval)
                        .minDocCount(0)
                        .extendedBounds(min, max)
                        .subAggregation(AggregationBuilders.extendedStats(AGG_EXTENDED_STATS).field(fieldName))
                        .subAggregation(AggregationBuilders.cardinality(AGG_CARDINALITY).field(fieldName))
                )
                .execute().actionGet();


        Histogram histogram = sr.getAggregations().get(histogramName);


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
            //System.out.println("key: " + key + ", doc_count: " + docCount);
        }

        return bucketStatResults;
    }

    private static String buildHistogramName(String indexName, String typeName, String fieldName){
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
