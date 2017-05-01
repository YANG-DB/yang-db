package com.kayhut.fuse.stat.Util;

import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.search.aggregations.AggregationBuilder;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.histogram.Histogram;

/**
 * Created by benishue on 01-May-17.
 */
public class EsUtil {

    public static void getNumericHistogram(TransportClient client,
                                           String indexName,
                                           String typeName,
                                           String fieldName,
                                           long min, long max,
                                           long interval){

        String histogramName = buildHistogramName(indexName,typeName,fieldName);
        SearchResponse sr =  client.prepareSearch(indexName)
                .setTypes(typeName)
                .addAggregation(AggregationBuilders.histogram(histogramName)
                        .field(fieldName)
                        .interval(interval)
                        .minDocCount(0)
                        .extendedBounds(min, max))
                .execute().actionGet();


        Histogram histogram = sr.getAggregations().get(histogramName);


        // For each entry
        for (Histogram.Bucket entry : histogram.getBuckets()) {
            Number key = (Number) entry.getKey();   // Key
            long docCount = entry.getDocCount();    // Doc count

            System.out.println("key: " + key + ", doc_count: " + docCount);
        }
    }

    private static String buildHistogramName(String indexName, String typeName, String fieldName){
        return indexName + "_" + typeName + "_" + fieldName + "_" + "hist";
    }
}
