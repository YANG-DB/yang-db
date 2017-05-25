package com.kayhut.fuse.epb.plan.statistics.provider;

import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.kayhut.fuse.epb.plan.statistics.GraphStatisticsProvider;
import com.kayhut.fuse.epb.plan.statistics.Statistics;
import com.kayhut.fuse.epb.plan.statistics.configuration.StatConfig;
import com.kayhut.fuse.epb.plan.statistics.util.ElasticUtil;
import com.kayhut.fuse.epb.plan.statistics.util.StatUtil;
import com.kayhut.fuse.model.query.Constraint;
import com.kayhut.fuse.model.query.ConstraintOp;
import com.kayhut.fuse.unipop.schemaProviders.GraphEdgeSchema;
import com.kayhut.fuse.unipop.schemaProviders.GraphElementPropertySchema;
import com.kayhut.fuse.unipop.schemaProviders.GraphElementSchema;
import com.kayhut.fuse.unipop.schemaProviders.GraphVertexSchema;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.index.query.*;
import org.elasticsearch.search.SearchHit;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Created by benishue on 22-May-17.
 */
public class ElasticStatisticsGraphProvider implements GraphStatisticsProvider {

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
//        graphVertexSchema.getType(); name of entity e.g., dragon
//        graphElementPropertySchema.getType()  // Type : String, integer -> BUCKET STRING, BUCKET NUMERIC
//        constraint.getOp() -> ELASTIC FILTER

        String entityType = graphVertexSchema.getType();
        String fieldType = graphElementPropertySchema.getType();
        String fieldName = graphElementPropertySchema.getName();
        ConstraintOp op = constraint.getOp();
//        switch (entityType) {
//            case STRING: {
//
//                break;
//            }
//            case gt: {
//                break;
//            }
//            case ge: {
//                break;
//            }
//            case lt: {
//                break;
//            }
//            case le: {
//                break;
//            }
//            default:
//                ;//todo
//
//        }




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
        long totalCount = 0;
        for (String index : indices) {
            totalCount += getTermBucketCount(index, docType, docType);
        }
        return new Statistics.Cardinality(totalCount, 0);
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
        Optional<Map<String, Object>> statDoc = ElasticUtil.getDocumentById(
                this.elasticClient,
                statConfig.getStatIndexName(),
                statConfig.getStatTermTypeName(),
                docId);
        if (statDoc.isPresent()) {
            count = (long) statDoc.get().get(statConfig.getStatCountFieldName());
        }
        return count;
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


    private void getHitFieldStatistics(Iterable<SearchHit> hits){
        List<SearchHit> hitList = Lists.newArrayList(hits);

//        if (hitList.get(0).getSource().get("dataType").equals("long")) {
//            converter = _longConverter;
//            if (true) {
//                converter = new TempLongElasticHitsToHistogramStatistics();
//            }
//        }
//        else if (hitList.get(0).getSource().get("dataType").equals("string")) {
//            converter = _stringConverter;
//        }
//
//        return converter.convertHits(hits);
    }


    //region Fields
    private StatConfig statConfig;
    private TransportClient elasticClient;
    private static final String DATE = "date";
    private static final String INT = "int";
    private static final String STRING = "string";
    //endregion

}
