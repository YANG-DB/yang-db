package com.yangdb.fuse.executor.elasticsearch.graph.actions;

import com.yangdb.fuse.executor.elasticsearch.graph.transport.GraphExploreRequest;
import com.yangdb.fuse.executor.elasticsearch.graph.transport.GraphExploreResponse;
import com.yangdb.fuse.executor.elasticsearch.graph.model.Hop;
import com.yangdb.fuse.executor.elasticsearch.graph.model.VertexRequest;
import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.aggregations.AggregationBuilder;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.significant.SignificantTermsAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.terms.IncludeExclude;
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;

import java.util.Optional;

public class AsyncGraphTermsTraversal extends GraphUtils{


    public AsyncGraphTermsTraversal(Client client, GraphExploreRequest request, ActionListener<GraphExploreResponse> listener) {
        super(client, request, listener);
    }

    /**
     * Step out from some existing vertex terms looking for useful
     * connections
     * @return
     */
    public synchronized Optional<GraphExploreResponse> expand() {
        if (hasTimedOut()) {
            timedOut.set(true);
            listener.onResponse(buildResponse());
            //async return type compromise
            return Optional.empty();
        }

        SearchRequestContext context = prepareSearchRequest();
        // System.out.println(source);
//            logger.trace("executing expansion graph search request");

        if(context.isComplete())
            return Optional.empty();

        //otherwise continue adding new neighbors
        client.search(context.searchRequest, new ActionListener<SearchResponse>() {
            @Override
            public void onResponse(SearchResponse searchResponse) {
                continueExpandAccordingToResponse(searchResponse,context.lastHop,context.currentHop);
            }

            @Override
            public void onFailure(Exception e) {
                listener.onFailure(e);
            }
        });
        //async return compromise
        return Optional.empty();
    }



    /**
     * For a given root query (or a set of "includes" root constraints) find
     * the related terms. These will be our start points in the graph
     * navigation.
     */
    public synchronized void start() {
        try {
            final SearchRequest searchRequest = buildSearchRequest();

            BoolQueryBuilder rootBool = QueryBuilders.boolQuery();

            AggregationBuilder rootSampleAgg = buildSampleAggregation();

            Hop rootHop = request.getHop(0);

            // Add any user-supplied criteria to the root query as a should clause
            rootBool.must(rootHop.guidingQuery());


            // If any of the root terms have an "include" restriction then
            // we add a root-level MUST clause that
            // mandates that at least one of the potentially many terms of
            // interest must be matched (using a should array)
            BoolQueryBuilder includesContainer = QueryBuilders.boolQuery();
            addUserDefinedIncludesToQuery(rootHop, includesContainer);
            if (includesContainer.should().size() > 0) {
                rootBool.must(includesContainer);
            }


            for (int i = 0; i < rootHop.getNumberVertexRequests(); i++) {
                VertexRequest vr = rootHop.getVertexRequest(i);
                if (request.useSignificance()) {
                    SignificantTermsAggregationBuilder sigBuilder = AggregationBuilders.significantTerms("field" + i);
                    sigBuilder.field(vr.fieldName()).shardMinDocCount(vr.shardMinDocCount()).minDocCount(vr.minDocCount())
                            // Map execution mode used because Sampler agg
                            // keeps us focused on smaller sets of high quality
                            // docs and therefore examine smaller volumes of terms
                            .executionHint("map").size(vr.size());
                    // It is feasible that clients could provide a choice of
                    // significance heuristic at some point e.g:
                    // sigBuilder.significanceHeuristic(new
                    // PercentageScore.PercentageScoreBuilder());

                    if (vr.hasIncludeClauses()) {
                        String[] includes = vr.includeValuesAsStringArray();
                        sigBuilder.includeExclude(new IncludeExclude(includes, null));
                        sigBuilder.size(includes.length);
                    }
                    if (vr.hasExcludeClauses()) {
                        sigBuilder.includeExclude(new IncludeExclude(null, vr.excludesAsArray()));
                    }
                    rootSampleAgg.subAggregation(sigBuilder);
                } else {
                    TermsAggregationBuilder termsBuilder = AggregationBuilders.terms("field" + i);
                    // Min doc count etc really only applies when we are
                    // thinking about certainty of significance scores -
                    // perhaps less necessary when considering popularity
                    // termsBuilder.field(vr.fieldName()).shardMinDocCount(shardMinDocCount)
                    //       .minDocCount(minDocCount).executionHint("map").size(vr.size());
                    termsBuilder.field(vr.fieldName()).executionHint("map").size(vr.size());
                    if (vr.hasIncludeClauses()) {
                        String[] includes = vr.includeValuesAsStringArray();
                        termsBuilder.includeExclude(new IncludeExclude(includes, null));
                        termsBuilder.size(includes.length);
                    }
                    if (vr.hasExcludeClauses()) {
                        termsBuilder.includeExclude(new IncludeExclude(null, vr.excludesAsArray()));
                    }
                    rootSampleAgg.subAggregation(termsBuilder);
                }
            }


            // Run the search
            SearchSourceBuilder source = new SearchSourceBuilder()
                    .query(rootBool)
                    .aggregation(rootSampleAgg).size(0);
            if (request.timeout() != null) {
                source.timeout(request.timeout());
            }
            searchRequest.source(source);
            // System.out.println(source);
//                logger.trace("executing initial graph search request");
            client.search(searchRequest, new ActionListener<SearchResponse>() {
                @Override
                public void onResponse(SearchResponse searchResponse) {
                    startSearchResponse(searchResponse, rootHop);
                }

                @Override
                public void onFailure(Exception e) {
                    listener.onFailure(e);
                }
            });
        } catch (Exception e) {
//                logger.error("unable to execute the graph query", e);
            listener.onFailure(e);
        }
    }
}
