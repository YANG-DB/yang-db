package com.yangdb.fuse.executor.elasticsearch.terms.actions;

/*-
 * #%L
 * fuse-dv-core
 * %%
 * Copyright (C) 2016 - 2020 The YangDb Graph Database Project
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import com.yangdb.fuse.executor.elasticsearch.terms.model.Edge;
import com.yangdb.fuse.executor.elasticsearch.terms.model.Step;
import com.yangdb.fuse.executor.elasticsearch.terms.transport.GraphExploreRequest;
import com.yangdb.fuse.executor.elasticsearch.terms.transport.GraphExploreResponse;
import com.yangdb.fuse.executor.elasticsearch.terms.model.Vertex;
import com.yangdb.fuse.executor.elasticsearch.terms.transport.VertexRequest;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.util.PriorityQueue;
import org.elasticsearch.ExceptionsHelper;
import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.ShardOperationFailedException;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.ShardSearchFailure;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.util.CollectionUtils;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.aggregations.AggregationBuilder;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.sampler.DiversifiedAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.sampler.Sampler;
import org.elasticsearch.search.aggregations.bucket.significant.SignificantTerms;
import org.elasticsearch.search.aggregations.bucket.significant.SignificantTermsAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.terms.IncludeExclude;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

public abstract class GraphUtils {

    protected final Client client;
    protected final GraphExploreRequest request;
    protected final ActionListener<GraphExploreResponse> listener;

    protected final long startTime;
    protected final AtomicBoolean timedOut;
    protected volatile ShardOperationFailedException[] shardFailures;
    protected Map<Vertex.VertexId, Vertex> vertices = new HashMap<>();
    protected Map<Edge.EdgeId, Edge> edges = new HashMap<>();

    // Each "hop" is recorded here using hopNumber->fieldName->vertices
    protected Map<Integer, Map<String, Set<Vertex>>> stepFindings = new HashMap<>();
    protected int currentStepNumber = 0;

    protected GraphUtils(Client client, GraphExploreRequest request) {
        this(client,request,null);
    }

    protected GraphUtils(Client client, GraphExploreRequest request, ActionListener<GraphExploreResponse> listener) {
        this.client = client;
        this.request = request;
        this.listener = listener;

        this.startTime = System.currentTimeMillis();
        this.timedOut = new AtomicBoolean(false);
        this.shardFailures = ShardSearchFailure.EMPTY_ARRAY;
    }

    public abstract Optional<GraphExploreResponse> expand();

    protected Vertex getVertex(String field, String term) {
        return vertices.get(Vertex.createId(field, term));
    }

    protected Edge addEdge(Vertex from, Vertex to, double weight, long docCount) {
        Edge edge = new Edge(from, to, weight, docCount);
        edges.put(edge.getId(), edge);
        return edge;
    }

    protected Vertex addVertex(String field, String term, double score, int depth, long bg, long fg) {
        Vertex.VertexId key = Vertex.createId(field, term);
        Vertex vertex = vertices.get(key);
        if (vertex == null) {
            vertex = new Vertex(field, term, score, depth, bg, fg);
            vertices.put(key, vertex);
            Map<String, Set<Vertex>> currentWave = stepFindings.computeIfAbsent(currentStepNumber, k -> new HashMap<>());
            Set<Vertex> verticesForField = currentWave.computeIfAbsent(field, k -> new HashSet<>());
            verticesForField.add(vertex);
        }
        return vertex;
    }

    protected void removeVertex(Vertex vertex) {
        vertices.remove(vertex.getId());
        stepFindings.get(currentStepNumber).get(vertex.getField()).remove(vertex);
    }

    public SearchRequest buildSearchRequest() {
        final SearchRequest searchRequest = new SearchRequest(request.indices()).indicesOptions(
                request.indicesOptions());
        if (request.routing() != null) {
            searchRequest.routing(request.routing());
        }
        return searchRequest;
    }

    protected SearchRequestContext prepareSearchRequest() {
        Map<String, Set<Vertex>> lastStepFindings = stepFindings.get(currentStepNumber);
        if ((currentStepNumber >= (request.getStepNumbers() - 1)) || (lastStepFindings == null) || (lastStepFindings.size() == 0)) {
            // Either we gathered no leads from the last hop or we have
            // reached the final hop
            return new SearchRequestContext(buildResponse());
        }

        Step lastStep = request.getStep(currentStepNumber);
        currentStepNumber++;
        Step currentStep = request.getStep(currentStepNumber);

        SearchRequest searchRequest = buildSearchRequest();

        BoolQueryBuilder rootBool = QueryBuilders.boolQuery();

        AggregationBuilder sampleAgg = buildSampleAggregation();

        // Add any user-supplied criteria to the root query as a must clause
        rootBool.must(currentStep.guidingQuery());

        // Build a MUST clause that matches one of either
        // a:) include clauses supplied by the client or
        // b:) vertex terms from the previous hop.
        BoolQueryBuilder sourceTermsOrClause = QueryBuilders.boolQuery();
        addUserDefinedIncludesToQuery(currentStep, sourceTermsOrClause);
        addBigOrClause(lastStepFindings, sourceTermsOrClause);

        rootBool.must(sourceTermsOrClause);


        //Now build the agg tree that will channel the content ->
        //   base agg is terms agg for terms from last wave (one per field),
        //      under each is a sig_terms agg to find next candidates (again, one per field)...
        buildAggTree(lastStepFindings, lastStep, currentStep, sampleAgg);

        // Execute the search
        SearchSourceBuilder source = new SearchSourceBuilder().query(rootBool).aggregation(sampleAgg).size(0);
        if (request.timeout() != null) {
            source.timeout(TimeValue.timeValueMillis(timeRemainingMillis()));
        }
        searchRequest.source(source);
        return new SearchRequestContext(searchRequest, lastStep, currentStep);
    }

    protected void buildAggTree(Map<String, Set<Vertex>> lastStepFindings, Step lastStep, Step currentStep, AggregationBuilder sampleAgg) {
        for (int fieldNum = 0; fieldNum < lastStep.getNumberVertexRequests(); fieldNum++) {
            VertexRequest lastVr = lastStep.getVertexRequest(fieldNum);
            Set<Vertex> lastWaveVerticesForField = lastStepFindings.get(lastVr.fieldName());
            if (lastWaveVerticesForField == null) {
                continue;
            }
            String[] terms = new String[lastWaveVerticesForField.size()];
            int i = 0;
            for (Vertex v : lastWaveVerticesForField) {
                terms[i++] = v.getTerm();
            }
            TermsAggregationBuilder lastWaveTermsAgg = AggregationBuilders.terms("field" + fieldNum)
                    .includeExclude(new IncludeExclude(terms, null))
                    .shardMinDocCount(1)
                    .field(lastVr.fieldName()).minDocCount(1)
                    // Map execution mode used because Sampler agg keeps us
                    // focused on smaller sets of high quality docs and therefore
                    // examine smaller volumes of terms
                    .executionHint("map")
                    .size(terms.length);
            sampleAgg.subAggregation(lastWaveTermsAgg);
            for (int f = 0; f < currentStep.getNumberVertexRequests(); f++) {
                VertexRequest vr = currentStep.getVertexRequest(f);
                int size = vr.size();
                if (vr.fieldName().equals(lastVr.fieldName())) {
                    //We have the potential for self-loops as we are looking at the same field so add 1 to the requested size
                    // because we need to eliminate fieldA:termA -> fieldA:termA links that are likely to be in the results.
                    size++;
                }
                if (request.useSignificance()) {
                    SignificantTermsAggregationBuilder nextWaveSigTerms = AggregationBuilders.significantTerms("field" + f)
                            .field(vr.fieldName())
                            .minDocCount(vr.minDocCount()).shardMinDocCount(vr.shardMinDocCount()).executionHint("map").size(size);
//                        nextWaveSigTerms.significanceHeuristic(new PercentageScore.PercentageScoreBuilder());
                    //Had some issues with no significant terms being returned when asking for small
                    // number of final results (eg 1) and only one shard. Setting shard_size higher helped.
                    if (size < 10) {
                        nextWaveSigTerms.shardSize(10);
                    }
                    // Alternative choices of significance algo didn't seem to be improvements....
//                        nextWaveSigTerms.significanceHeuristic(new GND.GNDBuilder(true));
//                        nextWaveSigTerms.significanceHeuristic(new ChiSquare.ChiSquareBuilder(false, true));

                    if (vr.hasIncludeClauses()) {
                        String[] includes = vr.includeValuesAsStringArray();
                        nextWaveSigTerms.includeExclude(new IncludeExclude(includes, null));
                        // Originally I thought users would always want the
                        // same number of results as listed in the include
                        // clause but it may be the only want the most
                        // significant e.g. in the lastfm example of
                        // plotting a single user's tastes and how that maps
                        // into a network showing only the most interesting
                        // band connections. So line below commented out

                        // nextWaveSigTerms.size(includes.length);

                    } else if (vr.hasExcludeClauses()) {
                        nextWaveSigTerms.includeExclude(new IncludeExclude(null, vr.excludesAsArray()));
                    }
                    lastWaveTermsAgg.subAggregation(nextWaveSigTerms);
                } else {
                    TermsAggregationBuilder nextWavePopularTerms = AggregationBuilders.terms("field" + f).field(vr.fieldName())
                            .minDocCount(vr.minDocCount()).shardMinDocCount(vr.shardMinDocCount())
                            // Map execution mode used because Sampler agg keeps us
                            // focused on smaller sets of high quality docs and therefore
                            // examine smaller volumes of terms
                            .executionHint("map")
                            .size(size);
                    if (vr.hasIncludeClauses()) {
                        String[] includes = vr.includeValuesAsStringArray();
                        nextWavePopularTerms.includeExclude(new IncludeExclude(includes, null));
                        // nextWavePopularTerms.size(includes.length);
                    } else if (vr.hasExcludeClauses()) {
                        nextWavePopularTerms.includeExclude(new IncludeExclude(null, vr.excludesAsArray()));
                    }
                    lastWaveTermsAgg.subAggregation(nextWavePopularTerms);
                }
            }
        }
    }

    protected AggregationBuilder buildSampleAggregation() {
        // A single sample pool of docs is built at the root of the aggs tree.
        // For quality's sake it might have made more sense to sample top docs
        // for each of the terms from the previous hop (e.g. an initial query for "beatles"
        // may have separate doc-sample pools for significant root terms "john", "paul", "yoko" etc)
        // but I found this dramatically slowed down execution - each pool typically had different docs which
        // each had non-overlapping sets of terms that needed frequencies looking up for significant terms.
        // A common sample pool reduces the specialization that can be given to each root term but
        // ultimately is much faster to run because of the shared vocabulary in a single sample set.
        AggregationBuilder sampleAgg = null;
        if (request.sampleDiversityField() != null) {
            DiversifiedAggregationBuilder diversifiedSampleAgg = AggregationBuilders.diversifiedSampler("sample")
                    .shardSize(request.sampleSize());
            diversifiedSampleAgg.field(request.sampleDiversityField());
            diversifiedSampleAgg.maxDocsPerValue(request.maxDocsPerDiversityValue());
            sampleAgg = diversifiedSampleAgg;
        } else {
            sampleAgg = AggregationBuilders.sampler("sample").shardSize(request.sampleSize());
        }
        return sampleAgg;
    }

    protected void addNormalizedBoosts(BoolQueryBuilder includesContainer, VertexRequest vr) {
        GraphExploreRequest.TermBoost[] termBoosts = vr.includeValues();


        if ((includesContainer.should().size() + termBoosts.length) > BooleanQuery.getMaxClauseCount()) {
            // Too many terms - we need a cheaper form of query to execute this
            List<String> termValues = new ArrayList<>();
            for (GraphExploreRequest.TermBoost tb : termBoosts) {
                termValues.add(tb.getTerm());
            }
            includesContainer.should(QueryBuilders.constantScoreQuery(QueryBuilders.termsQuery(vr.fieldName(), termValues)));
            return;

        }
        // We have a sufficiently low number of terms to use the per-term boosts.
        // Lucene boosts are >=1 so we baseline the provided boosts to start
        // from 1
        float minBoost = Float.MAX_VALUE;
        for (GraphExploreRequest.TermBoost tb : termBoosts) {
            minBoost = Math.min(minBoost, tb.getBoost());
        }
        for (GraphExploreRequest.TermBoost tb : termBoosts) {
            float normalizedBoost = tb.getBoost() / minBoost;
            includesContainer.should(QueryBuilders.termQuery(vr.fieldName(), tb.getTerm()).boost(normalizedBoost));
        }
    }

    protected boolean hasTimedOut() {
        return request.timeout() != null && (timeRemainingMillis() <= 0);
    }

    protected long timeRemainingMillis() {
        // Actual resolution of timer is granularity of the interval
        // configured globally for updating estimated time.
        return (startTime + request.timeout().millis()) - System.currentTimeMillis();
    }

    protected void addShardFailures(ShardOperationFailedException[] failures) {
        if (!CollectionUtils.isEmpty(failures)) {
            ShardOperationFailedException[] duplicates = new ShardOperationFailedException[shardFailures.length + failures.length];
            System.arraycopy(shardFailures, 0, duplicates, 0, shardFailures.length);
            System.arraycopy(failures, 0, duplicates, shardFailures.length, failures.length);
            shardFailures = ExceptionsHelper.groupBy(duplicates);
        }
    }

    protected GraphExploreResponse buildResponse() {
        long took = System.currentTimeMillis() - startTime;
        return new GraphExploreResponse(took, timedOut.get(), shardFailures, vertices, edges, request.returnDetailedInfo());
    }

    public Optional<GraphExploreResponse> startSearchResponse(SearchResponse searchResponse, Step rootStep) {
        addShardFailures(searchResponse.getShardFailures());
        Sampler sample = searchResponse.getAggregations().get("sample");

        // Determine the total scores for all interesting terms
        double totalSignalStrength = getInitialTotalSignalStrength(rootStep, sample);


        // Now gather the best matching terms and compute signal weight according to their
        // share of the total signal strength
        for (int j = 0; j < rootStep.getNumberVertexRequests(); j++) {
            VertexRequest vr = rootStep.getVertexRequest(j);
            if (request.useSignificance()) {
                SignificantTerms significantTerms = sample.getAggregations().get("field" + j);
                List<? extends SignificantTerms.Bucket> buckets = significantTerms.getBuckets();
                for (SignificantTerms.Bucket bucket : buckets) {
                    double signalWeight = bucket.getSignificanceScore() / totalSignalStrength;
                    addVertex(vr.fieldName(), bucket.getKeyAsString(), signalWeight,
                            currentStepNumber, bucket.getSupersetDf(), bucket.getSubsetDf());
                }
            } else {
                Terms terms = sample.getAggregations().get("field" + j);
                List<? extends Terms.Bucket> buckets = terms.getBuckets();
                for (Terms.Bucket bucket : buckets) {
                    double signalWeight = bucket.getDocCount() / totalSignalStrength;
                    addVertex(vr.fieldName(), bucket.getKeyAsString(), signalWeight, currentStepNumber, 0, 0);
                }
            }
        }
        // Expand out from these root vertices looking for connections with other terms
        return expand();
    }

    public Optional<GraphExploreResponse> continueExpandAccordingToResponse(SearchResponse searchResponse, Step lastStep, Step currentStep) {
        // System.out.println(searchResponse);
        addShardFailures(searchResponse.getShardFailures());

        ArrayList<Edge> newEdges = new ArrayList<Edge>();
        ArrayList<Vertex> newVertices = new ArrayList<Vertex>();
        Sampler sample = searchResponse.getAggregations().get("sample");

        // We think of the total scores as the energy-level pouring
        // out of all the last hop's connections.
        // Each new node encountered is given a score which is
        // normalized between zero and one based on
        // what percentage of the total scores its own score
        // provides
        double totalSignalOutput = getExpandTotalSignalStrength(lastStep, currentStep, sample);

        // Signal output can be zero if we did not encounter any new
        // terms as part of this stage
        if (totalSignalOutput > 0) {
            addAndScoreNewVertices(lastStep, currentStep, sample, totalSignalOutput, newEdges, newVertices);

            trimNewAdditions(currentStep, newEdges, newVertices);
        }

        // Potentially run another round of queries to perform next"hop" - will terminate if no new additions
        return expand();
    }

    //TODO right now we only trim down to the best N vertices. We might also want to offer
    // clients the option to limit to the best M connections. One scenario where this is required
    // is if the "from" and "to" nodes are a client-supplied set of includes e.g. a list of
    // music artists then the client may be wanting to draw only the most-interesting connections
    // between them. See https://github.com/elastic/x-plugins/issues/518#issuecomment-160186424
    // I guess clients could trim the returned connections (which all have weights) but I wonder if
    // we can do something server-side here

    // Helper method - compute the total signal of all scores in the search results
    protected double getExpandTotalSignalStrength(Step lastStep, Step currentStep, Sampler sample) {
        double totalSignalOutput = 0;
        for (int j = 0; j < lastStep.getNumberVertexRequests(); j++) {
            VertexRequest lastVr = lastStep.getVertexRequest(j);
            Terms lastWaveTerms = sample.getAggregations().get("field" + j);
            if (lastWaveTerms == null) {
                continue;
            }
            List<? extends Terms.Bucket> buckets = lastWaveTerms.getBuckets();
            for (Terms.Bucket lastWaveTerm : buckets) {
                for (int k = 0; k < currentStep.getNumberVertexRequests(); k++) {
                    VertexRequest vr = currentStep.getVertexRequest(k);
                    if (request.useSignificance()) {
                        // Signal is based on significance score
                        SignificantTerms significantTerms = lastWaveTerm.getAggregations().get("field" + k);
                        if (significantTerms != null) {
                            for (SignificantTerms.Bucket bucket : significantTerms.getBuckets()) {
                                if ((vr.fieldName().equals(lastVr.fieldName()))
                                        && (bucket.getKeyAsString().equals(lastWaveTerm.getKeyAsString()))) {
                                    // don't count self joins (term A obviously co-occurs with term A)
                                    continue;
                                } else {
                                    totalSignalOutput += bucket.getSignificanceScore();
                                }
                            }
                        }
                    } else {
                        // Signal is based on popularity (number of
                        // documents)
                        Terms terms = lastWaveTerm.getAggregations().get("field" + k);
                        if (terms != null) {
                            for (Terms.Bucket bucket : terms.getBuckets()) {
                                if ((vr.fieldName().equals(lastVr.fieldName()))
                                        && (bucket.getKeyAsString().equals(lastWaveTerm.getKeyAsString()))) {
                                    // don't count self joins (term A obviously co-occurs with term A)
                                    continue;
                                } else {
                                    totalSignalOutput += bucket.getDocCount();
                                }
                            }
                        }
                    }
                }
            }
        }
        return totalSignalOutput;
    }

    // Add new vertices and apportion share of total signal along
    // connections
    protected void addAndScoreNewVertices(Step lastStep, Step currentStep, Sampler sample, double totalSignalOutput,
                                          ArrayList<Edge> newEdges, ArrayList<Vertex> newVertices) {
        // Gather all matching terms into the graph and propagate
        // signals
        for (int j = 0; j < lastStep.getNumberVertexRequests(); j++) {
            VertexRequest lastVr = lastStep.getVertexRequest(j);
            Terms lastWaveTerms = sample.getAggregations().get("field" + j);
            if (lastWaveTerms == null) {
                // There were no terms from the previous phase that needed pursuing
                continue;
            }
            List<? extends Terms.Bucket> buckets = lastWaveTerms.getBuckets();
            for (Terms.Bucket lastWaveTerm : buckets) {
                Vertex fromVertex = getVertex(lastVr.fieldName(), lastWaveTerm.getKeyAsString());
                for (int k = 0; k < currentStep.getNumberVertexRequests(); k++) {
                    VertexRequest vr = currentStep.getVertexRequest(k);
                    // As we travel further out into the graph we apply a
                    // decay to the signals being propagated down the various channels.
                    double decay = 0.95d;
                    if (request.useSignificance()) {
                        SignificantTerms significantTerms = lastWaveTerm.getAggregations().get("field" + k);
                        if (significantTerms != null) {
                            for (SignificantTerms.Bucket bucket : significantTerms.getBuckets()) {
                                if ((vr.fieldName().equals(fromVertex.getField())) &&
                                        (bucket.getKeyAsString().equals(fromVertex.getTerm()))) {
                                    // Avoid self-joins
                                    continue;
                                }
                                double signalStrength = bucket.getSignificanceScore() / totalSignalOutput;

                                // Decay the signal by the weight attached to the source vertex
                                signalStrength = signalStrength * Math.min(decay, fromVertex.getWeight());

                                Vertex toVertex = getVertex(vr.fieldName(), bucket.getKeyAsString());
                                if (toVertex == null) {
                                    toVertex = addVertex(vr.fieldName(), bucket.getKeyAsString(), signalStrength,
                                            currentStepNumber, bucket.getSupersetDf(), bucket.getSubsetDf());
                                    newVertices.add(toVertex);
                                } else {
                                    toVertex.setWeight(toVertex.getWeight() + signalStrength);
                                    // We cannot (without further querying) determine an accurate number
                                    // for the foreground count of the toVertex term - if we sum the values
                                    // from each fromVertex term we may actually double-count occurrences so
                                    // the best we can do is take the maximum foreground value we have observed
                                    toVertex.setFg(Math.max(toVertex.getFg(), bucket.getSubsetDf()));
                                }
                                newEdges.add(addEdge(fromVertex, toVertex, signalStrength, bucket.getDocCount()));
                            }
                        }
                    } else {
                        Terms terms = lastWaveTerm.getAggregations().get("field" + k);
                        if (terms != null) {
                            for (Terms.Bucket bucket : terms.getBuckets()) {
                                double signalStrength = bucket.getDocCount() / totalSignalOutput;
                                // Decay the signal by the weight attached to the source vertex
                                signalStrength = signalStrength * Math.min(decay, fromVertex.getWeight());

                                Vertex toVertex = getVertex(vr.fieldName(), bucket.getKeyAsString());
                                if (toVertex == null) {
                                    toVertex = addVertex(vr.fieldName(), bucket.getKeyAsString(), signalStrength,
                                            currentStepNumber, 0, 0);
                                    newVertices.add(toVertex);
                                } else {
                                    toVertex.setWeight(toVertex.getWeight() + signalStrength);
                                }
                                newEdges.add(addEdge(fromVertex, toVertex, signalStrength, bucket.getDocCount()));
                            }
                        }
                    }
                }
            }
        }
    }


    // Having let the signals from the last results rattle around the graph
    // we have adjusted weights for the various vertices we encountered.
    // Now we review these new additions and remove those with the
    // weakest weights.
    // A priority queue is used to trim vertices according to the size settings
    // requested for each field.
    protected void trimNewAdditions(Step currentStep, ArrayList<Edge> newEdges, ArrayList<Vertex> newVertices) {
        Set<Vertex> evictions = new HashSet<>();

        for (int k = 0; k < currentStep.getNumberVertexRequests(); k++) {
            // For each of the fields
            VertexRequest vr = currentStep.getVertexRequest(k);
            if (newVertices.size() <= vr.size()) {
                // Nothing to trim
                continue;
            }
            // Get the top vertices for this field
            VertexPriorityQueue pq = new VertexPriorityQueue(vr.size());
            for (Vertex vertex : newVertices) {
                if (vertex.getField().equals(vr.fieldName())) {
                    Vertex eviction = pq.insertWithOverflow(vertex);
                    if (eviction != null) {
                        evictions.add(eviction);
                    }
                }
            }
        }
        // Remove weak new nodes and their dangling connections from the main graph
        if (evictions.size() > 0) {
            for (Edge edge : newEdges) {
                if (evictions.contains(edge.getTo())) {
                    edges.remove(edge.getId());
                    removeVertex(edge.getTo());
                }
            }
        }
    }

    protected void addBigOrClause(Map<String, Set<Vertex>> lastStepFindings, BoolQueryBuilder sourceTermsOrClause) {
        int numClauses = sourceTermsOrClause.should().size();
        for (Map.Entry<String, Set<Vertex>> entry : lastStepFindings.entrySet()) {
            numClauses += entry.getValue().size();
        }
        if (numClauses < BooleanQuery.getMaxClauseCount()) {
            // We can afford to build a Boolean OR query with individual
            // boosts for interesting terms
            for (Map.Entry<String, Set<Vertex>> entry : lastStepFindings.entrySet()) {
                for (Vertex vertex : entry.getValue()) {
                    sourceTermsOrClause.should(
                            QueryBuilders.constantScoreQuery(
                                    QueryBuilders.termQuery(vertex.getField(), vertex.getTerm())).boost((float) vertex.getWeight()));
                }
            }

        } else {
            // Too many terms - we need a cheaper form of query to execute this
            for (Map.Entry<String, Set<Vertex>> entry : lastStepFindings.entrySet()) {
                List<String> perFieldTerms = new ArrayList<>();
                for (Vertex vertex : entry.getValue()) {
                    perFieldTerms.add(vertex.getTerm());
                }
                sourceTermsOrClause.should(QueryBuilders.constantScoreQuery(QueryBuilders.termsQuery(entry.getKey(), perFieldTerms)));
            }
        }
    }

    protected void addUserDefinedIncludesToQuery(Step step, BoolQueryBuilder sourceTermsOrClause) {
        for (int i = 0; i < step.getNumberVertexRequests(); i++) {
            VertexRequest vr = step.getVertexRequest(i);
            if (vr.hasIncludeClauses()) {
                addNormalizedBoosts(sourceTermsOrClause, vr);
            }
        }
    }

    protected double getInitialTotalSignalStrength(Step rootStep, Sampler sample) {
        double totalSignalStrength = 0;
        for (int i = 0; i < rootStep.getNumberVertexRequests(); i++) {
            if (request.useSignificance()) {
                // Signal is based on significance score
                SignificantTerms significantTerms = sample.getAggregations().get("field" + i);
                List<? extends SignificantTerms.Bucket> buckets = significantTerms.getBuckets();
                for (SignificantTerms.Bucket bucket : buckets) {
                    totalSignalStrength += bucket.getSignificanceScore();
                }
            } else {
                // Signal is based on popularity (number of documents)
                Terms terms = sample.getAggregations().get("field" + i);
                List<? extends Terms.Bucket> buckets = terms.getBuckets();
                for (Terms.Bucket bucket : buckets) {
                    totalSignalStrength += bucket.getDocCount();
                }
            }
        }
        return totalSignalStrength;
    }

    public static class SearchRequestContext {
        public SearchRequest searchRequest;
        public Step lastStep;
        public Step currentStep;
        public GraphExploreResponse response;

        public SearchRequestContext(SearchRequest searchRequest, Step lastStep, Step currentStep) {
            this.searchRequest = searchRequest;
            this.lastStep = lastStep;
            this.currentStep = currentStep;
        }

        public SearchRequestContext(GraphExploreResponse response) {
            this.response = response;
        }

        public boolean isComplete() {
            return this.response != null;
        }
    }

    public static class VertexPriorityQueue extends PriorityQueue<Vertex> {

        public VertexPriorityQueue(int maxSize) {
            super(maxSize);
        }

        @Override
        protected boolean lessThan(Vertex a, Vertex b) {
            return a.getWeight() < b.getWeight();
        }

    }

}