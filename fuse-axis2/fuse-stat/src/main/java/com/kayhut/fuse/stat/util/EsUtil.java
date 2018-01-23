package com.kayhut.fuse.stat.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kayhut.fuse.stat.model.bucket.BucketRange;
import com.kayhut.fuse.stat.model.bucket.BucketTerm;
import com.kayhut.fuse.stat.model.configuration.Filter;
import com.kayhut.fuse.stat.model.enums.DataType;
import com.kayhut.fuse.stat.model.result.StatGlobalCardinalityResult;
import com.kayhut.fuse.stat.model.result.StatRangeResult;
import com.kayhut.fuse.stat.model.result.StatTermResult;
import javaslang.Tuple2;
import javaslang.collection.Stream;
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
import org.elasticsearch.search.aggregations.bucket.filter.Filters;
import org.elasticsearch.search.aggregations.bucket.filter.FiltersAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.filter.FiltersAggregator;
import org.elasticsearch.search.aggregations.bucket.range.Range;
import org.elasticsearch.search.aggregations.bucket.range.RangeAggregationBuilder;
import org.elasticsearch.search.aggregations.metrics.cardinality.InternalCardinality;
import org.elasticsearch.search.aggregations.metrics.stats.extended.ExtendedStats;
import org.elasticsearch.search.aggregations.metrics.stats.extended.ExtendedStatsAggregationBuilder;

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
     * @param client    Elastic client
     * @param index     Elastic index name (e.g., index1)
     * @param type      Elastic type name (e.g., Dragon)
     * @param field     Elastic field name
     * @return List of numeric range buckets with lower (inclusive) and upper bound (exclusive)
     */
    public static List<StatRangeResult<? extends Number>> getNumericHistogramResults(TransportClient client,
                                                                   String index,
                                                                   String type,
                                                                   String field,
                                                                   DataType dataType,
                                                                   List<BucketRange<? extends Number>> buckets) {


        return getNumericBucketsStatResults(client, index, type, field, dataType, buckets);
    }

    public static <T> List<StatRangeResult<T>> getManualHistogramResults(TransportClient client,
                                                                      String index,
                                                                      String type,
                                                                      String field,
                                                                      DataType dataType,
                                                                      List<BucketRange<T>> buckets) {
        if (dataType == DataType.string) {
            List<BucketRange<String>> stringBucketRanges = new ArrayList<>();
            buckets.forEach(bucketRange -> stringBucketRanges.add(new BucketRange<>((String) bucketRange.getStart(), (String) bucketRange.getEnd())));
            return Stream.ofAll(getStringBucketsStatResults(client, index, type, field, stringBucketRanges))
                    .map(statRangeResult -> (StatRangeResult<T>)statRangeResult).toJavaList();
        }

        if (dataType == DataType.numericDouble || dataType == DataType.numericLong) {
            List<BucketRange<? extends Number>> numericBucketRanges = new ArrayList<>();
            buckets.forEach(bucketRange -> numericBucketRanges.add(new BucketRange<>((Number) bucketRange.getStart(), (Number) bucketRange.getEnd())));
            return Stream.ofAll(getNumericBucketsStatResults(client, index, type, field, dataType, numericBucketRanges))
                    .map(statRangeResult -> (StatRangeResult<T>)statRangeResult).toJavaList();
        }

        return Collections.emptyList();
    }

    public static <T> List<StatGlobalCardinalityResult> getGlobalCardinalityHistogramResults(TransportClient client,
                                                                                  String index,
                                                                                  String type,
                                                                                  String field,
                                                                                  String direction,
                                                                                  List<BucketRange<T>> buckets) {

        List<StatGlobalCardinalityResult> bucketStatResults = new ArrayList<>();

        String aggName = buildAggName(index, type, field + "_direction_" + direction);
        SearchRequestBuilder searchRequestBuilder = client.prepareSearch(index)
                .setQuery(QueryBuilders.constantScoreQuery(QueryBuilders.termQuery("type", type)));

        FiltersAggregationBuilder filtersAggregationBuilder = AggregationBuilders.filters(aggName,
        Stream.ofAll(buckets).map(bucket -> {
            String bucketKey = bucket.getStart() + "_" + bucket.getEnd();
            return new FiltersAggregator.KeyedFilter(bucketKey, QueryBuilders.boolQuery()
                    .must(QueryBuilders.rangeQuery(field)
                    .from(bucket.getStart())
                    .to(bucket.getEnd())
                    .includeLower(true)
                    .includeUpper(true))// in this case - inclusive
                    .must(QueryBuilders.termQuery("direction", direction)));
        }).toJavaArray(FiltersAggregator.KeyedFilter.class));

        SearchResponse sr = searchRequestBuilder.addAggregation(filtersAggregationBuilder
                .subAggregation(AggregationBuilders.cardinality(AGG_CARDINALITY).field(field)))
                .setSize(0).execute().actionGet();

        Filters aggregation = sr.getAggregations().get(aggName);

        for (Filters.Bucket entry : aggregation.getBuckets()) {
            InternalCardinality cardinality = entry.getAggregations().get(AGG_CARDINALITY);

            StatGlobalCardinalityResult bucketStatResult = new StatGlobalCardinalityResult(index, type, field,
                    direction,
                    entry.getDocCount(),
                    cardinality.getValue());

            bucketStatResults.add(bucketStatResult);
        }
        return bucketStatResults;
    }


    /**
     * @param client  Elastic client
     * @param index   Elastic index name (e.g., index1)
     * @param type    Elastic type name (e.g., Dragon)
     * @param field   Elastic field name (e.g., Address)
     * @param buckets - String buckets ["str1", "str2")
     * @return List of String range buckets with lower (inclusive) and upper bound (exclusive)
     */
    public static List<StatRangeResult> getStringBucketsStatResults(TransportClient client,
                                                                    String index,
                                                                    String type,
                                                                    String field,
                                                                    List<BucketRange<String>> buckets) {

        List<StatRangeResult> bucketStatResults = new ArrayList<>();

        String aggName = buildAggName(index, type, field);
        SearchRequestBuilder searchRequestBuilder = client.prepareSearch(index)
                .setQuery(QueryBuilders.constantScoreQuery(QueryBuilders.termQuery("type", type)));


        FiltersAggregationBuilder filtersAggregationBuilder = AggregationBuilders.filters(aggName,
        Stream.ofAll(buckets).map(bucket -> {
            String bucketKey = bucket.getStart() + "_" + bucket.getEnd();
            return new FiltersAggregator.KeyedFilter(bucketKey, QueryBuilders.rangeQuery(field).
                    from(bucket.getStart()).to(bucket.getEnd())
                    .includeLower(true)
                    .includeUpper(false));
        }).toJavaArray(FiltersAggregator.KeyedFilter.class));

        Map<String, BucketRange<String>> bucketsByKey = Stream.ofAll(buckets).toJavaMap(bucket -> new Tuple2<>(bucket.getStart() + "_" + bucket.getEnd(), bucket));

        SearchResponse sr = searchRequestBuilder.addAggregation(filtersAggregationBuilder
                .subAggregation(AggregationBuilders.cardinality(AGG_CARDINALITY).field(field)))
                .setSize(0).execute().actionGet();

        Filters aggregation = sr.getAggregations().get(aggName);

        for (Filters.Bucket entry : aggregation.getBuckets()) {
            BucketRange<String> stringBucketRange = bucketsByKey.get(entry.getKeyAsString());
            String start = stringBucketRange.getStart();          // Bucket start
            String end = stringBucketRange.getEnd();              // Bucket end
            InternalCardinality cardinality = entry.getAggregations().get(AGG_CARDINALITY);

            StatRangeResult bucketStatResult = new StatRangeResult(index, type, field,
                    entry.getKeyAsString(),
                    DataType.string,
                    start,
                    end,
                    entry.getDocCount(),
                    cardinality.getValue());

            bucketStatResults.add(bucketStatResult);
        }
        return bucketStatResults;
    }

    public static List<StatTermResult> getTermHistogramResults(TransportClient client,
                                                               String index,
                                                               String type,
                                                               String field,
                                                               DataType dataType,
                                                               List<BucketTerm> buckets) {

        List<StatTermResult> bucketStatResults = new ArrayList<>();

        String aggName = buildAggName(index, type, field);
        SearchRequestBuilder searchRequestBuilder = client.prepareSearch(index)
                .setQuery(QueryBuilders.constantScoreQuery(QueryBuilders.termQuery("type", type)));

        FiltersAggregationBuilder filtersAggregationBuilder = AggregationBuilders.filters(aggName,
            Stream.ofAll(buckets)
                .map(bucket -> new FiltersAggregator.KeyedFilter((String) bucket.getTerm(), QueryBuilders.termQuery(field, bucket.getTerm())))
                .toJavaArray(FiltersAggregator.KeyedFilter.class));

        SearchResponse sr = searchRequestBuilder.addAggregation(filtersAggregationBuilder
                .subAggregation(AggregationBuilders.cardinality(AGG_CARDINALITY).field(field)))
                .setSize(0).execute().actionGet();

        Filters aggregation = sr.getAggregations().get(aggName);

        for (Filters.Bucket entry : aggregation.getBuckets()) {
            InternalCardinality cardinality = entry.getAggregations().get(AGG_CARDINALITY);

            StatTermResult bucketStatResult = new StatTermResult(index, type, field,
                    entry.getKeyAsString(),
                    dataType,
                    entry.getKeyAsString(),
                    entry.getDocCount(),
                    cardinality.getValue());

            bucketStatResults.add(bucketStatResult);
        }
        return bucketStatResults;
    }

    /**
     * @param client  Elastic client
     * @param index   Elastic index name (e.g., index1)
     * @param type    Elastic type name (e.g., Dragon)
     * @param field   Elastic field name
     * @param dataType
     * @param numOfBins
     * @return
     */
    public static <T> List<StatRangeResult<T>> getDynamicHistogramResults(TransportClient client,
                                                                   String index,
                                                                   String type,
                                                                   String field,
                                                                   DataType dataType,
                                                                   int numOfBins) {

        SearchRequestBuilder searchRequestBuilder = client.prepareSearch(index)
                .setQuery(QueryBuilders.constantScoreQuery(QueryBuilders.termQuery("type", type)));

        ExtendedStatsAggregationBuilder metricAgg = AggregationBuilders
                .extendedStats(AGG_EXTENDED_STATS)
                .field(field);

        SearchResponse sr = searchRequestBuilder.addAggregation(metricAgg)
                .setSize(0)
                .execute()
                .actionGet();

        ExtendedStats agg = sr.getAggregations().get(AGG_EXTENDED_STATS);

        List<StatRangeResult<T>> statResults = new ArrayList<>();
        List<BucketRange<? extends Number>> buckets;
        if (dataType == DataType.numericDouble) {
            buckets = new ArrayList<>(StatUtil.createDoubleBuckets(agg.getMin(), agg.getMax(), numOfBins));
            statResults = Stream.ofAll(getNumericBucketsStatResults(client, index, type, field, dataType, buckets))
                    .map(statRangeResult -> (StatRangeResult<T>)statRangeResult)
                    .toJavaList();
        } else if (dataType == DataType.numericLong) {
            buckets = new ArrayList<>(StatUtil.createLongBuckets(((Number)agg.getMin()).longValue(), ((Number)agg.getMax()).longValue(), numOfBins));
            statResults = Stream.ofAll(getNumericBucketsStatResults(client, index, type, field, dataType, buckets))
                    .map(statRangeResult -> (StatRangeResult<T>)statRangeResult)
                    .toJavaList();
        }

        return statResults;
    }

    /**
     * @param client Elastic Client
     * @param index  Index Name
     * @return true if an index exists
     */
    public static boolean isIndexExists(Client client, String index) {

        IndexMetaData indexMetaData = client.admin().cluster()
                .state(Requests.clusterStateRequest())
                .actionGet()
                .getState()
                .getMetaData()
                .index(index);

        return indexMetaData != null;

    }

    /**
     * @param client Elastic Client
     * @param index  Index Name
     * @param type   Type Name
     * @return true if type exists in the index
     */
    public static boolean isTypeExists(Client client, String index, String type) {
        SearchResponse response = client.prepareSearch().setIndices(index)
                .setQuery(QueryBuilders.constantScoreQuery(QueryBuilders.termQuery("type", type)))
                .setSize(1)
                .execute().actionGet();

        return response.getHits().getTotalHits() > 0;
    }

    public static boolean isDocExists(Client client, String index, String type, String docId) {
        Optional<String> actualType = getDocumentTypeByDocId(client, index, docId);
        return actualType.map(s -> s.equals(type)).orElse(false);
    }

    /**
     * @param client Elastic Client
     * @param index  Index Name
     * @param type   Type Name
     * @param id     Document Id
     * @return Elastic source document (As a map)
     */
    public static Optional<Map<String, Object>> getDocumentSourceById(Client client,
                                                                      String index,
                                                                      String type,
                                                                      String id) {
        GetResponse r = getGetResponse(client, index, id);
        if (r != null && r.isExists()) {
            return Optional.ofNullable(r.getSourceAsMap());
        }
        return Optional.empty();
    }

    /**
     * @param client Elastic Client
     * @param index  Index Name
     * @param id     Document Id
     * @return Elastic Type
     */
    public static Optional<String> getDocumentTypeByDocId(Client client, String index, String id) {
        GetResponse r = getGetResponse(client, index, id);
        if (r != null && r.isExists()) {
            try {
                String actualType = (String) r.getSource().get("type");
                return Optional.of(actualType);
            } catch (Exception ex) {
                return Optional.empty();
            }
        }
        return Optional.empty();
    }

    /**
     * Return all the documents from a type.
     *
     * @param client Elastic Client
     * @param index  Index Name
     * @param type   Type Name
     * @param n      number of documents
     * @return n first documents
     */
    public static SearchResponse getFirstNDocumentsInType(Client client,
                                                          String index,
                                                          String type,
                                                          int n) {
        return client.prepareSearch(index)
                .setQuery(QueryBuilders.boolQuery().filter(QueryBuilders.termQuery("type", type)))
                .setSize(n)
                .execute()
                .actionGet();
    }

    /**
     * Return all the documents from a cluster.
     *
     * @param client Elastic Client
     * @return Elastic search response
     */
    public static SearchResponse getAllDocuments(Client client) {
        return client.prepareSearch().execute().actionGet();
    }

    /**
     * Return all indices from cluster.
     *
     * @param client Elastic Client
     * @return array of Indices
     */
    public static String[] getAllIndices(Client client) {
        Set<String> indicesSet = client.admin().indices().stats(new IndicesStatsRequest())
                .actionGet().getIndices().keySet();
        return indicesSet.toArray(new String[indicesSet.size()]);
    }

    /**
     * Return all mappings of given index
     *
     * @param client Elastic Client
     * @param index  Index Name
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
     *
     * @param client Elastic Client
     * @param index  Index Name
     * @return array of Elastic types
     */
    public static String[] getAllTypesFromIndex(
            Client client, String index) {
        return getMappingsOfIndex(client, index).keys().toArray(String.class);
    }

    /**
     * Index given document.
     *
     * @param client:   Client used to index data
     * @param index:    Document is stored in this index
     * @param type:     Document stored in this type
     * @param id:       Specifies _id of the document
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
     *
     * @param client: Client used to index data
     * @param index:  Document is stored in this index
     * @param type:   Document stored in this type
     * @param id      : Specifies id of the document
     * @param obj:    Object to index
     * @return {@link IndexResponse}
     */
    public static IndexResponse indexData(Client client, String index,
                                          String type, String id, Object obj) throws JsonProcessingException {
        return indexData(client, index, type, id, new ObjectMapper().writeValueAsString(obj));
    }

    private static GetResponse getGetResponse(Client client,
                                              String index,
                                              String id) {
        return client.prepareGet()
                .setIndex(index)
                .setId(id)
                .setFetchSource(true)
                .setRefresh(true)
                .execute()
                .actionGet();
    }

    private static String buildAggName(String index,
                                       String type,
                                       String field) {
        return String.format("%s_%s_%s_hist", index, type, field);
    }

    /**
     * @param client  Elastic client
     * @param index   Elastic index name (e.g., index1)
     * @param type    Elastic type name (e.g., Dragon)
     * @param field   Elastic field name
     * @param buckets List of numeric buckets with each bucket [lower bound, upper bound]
     * @return Numeric buckets with lower bound (inclusive),  upper bound (exclusive)
     */
    private static List<StatRangeResult<? extends Number>> getNumericBucketsStatResults(Client client,
                                                                      String index,
                                                                      String type,
                                                                      String field,
                                                                      DataType dataType,
                                                                      List<BucketRange<? extends Number>> buckets) {
        List<StatRangeResult<? extends Number>> bucketStatResults = new ArrayList<>();
        String aggName = buildAggName(index, type, field);

        SearchRequestBuilder searchRequestBuilder = client.prepareSearch(index)
                .setQuery(QueryBuilders.constantScoreQuery(QueryBuilders.termQuery("type", type)));

        RangeAggregationBuilder rangesAggregationBuilder = AggregationBuilders.range(aggName).field(field);
        buckets.forEach(bucket -> rangesAggregationBuilder.addRange(bucket.getStart().doubleValue(), bucket.getEnd().doubleValue()));

        SearchResponse sr = searchRequestBuilder.addAggregation(rangesAggregationBuilder
                .subAggregation(AggregationBuilders.cardinality(AGG_CARDINALITY).field(field)))
                .setSize(0).execute().actionGet();

        Range agg = sr.getAggregations().get(aggName);

        for (Range.Bucket entry : agg.getBuckets()) {
            String key = entry.getKeyAsString();             // Range as key
            Number from = (Number) entry.getFrom();          // Bucket from
            Number to = (Number) entry.getTo();              // Bucket to

            InternalCardinality cardinality = entry.getAggregations().get(AGG_CARDINALITY);
            StatRangeResult<? extends Number> bucketStatResult = new StatRangeResult<>(index, type, field,
                    key,
                    dataType,
                    from,
                    to,
                    entry.getDocCount(),
                    cardinality.getValue());

            bucketStatResults.add(bucketStatResult);
        }

        return bucketStatResults;
    }
}
