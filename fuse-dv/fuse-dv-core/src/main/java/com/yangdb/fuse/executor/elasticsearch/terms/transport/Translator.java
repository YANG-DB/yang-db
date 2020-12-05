package com.yangdb.fuse.executor.elasticsearch.terms.transport;

import com.yangdb.fuse.executor.elasticsearch.terms.model.Step;
import com.yangdb.fuse.executor.elasticsearch.terms.model.Vertex;
import com.yangdb.fuse.model.transport.TermsExplorationRequest;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static org.elasticsearch.index.query.QueryBuilders.termQuery;

/**
 * translate the 'wire-protocol' request transport object into the driver request object
 */
public interface Translator {
    /**
     * translate the 'wire-protocol' request transport object into the driver request object
     *
     * @param request
     * @return
     */
    static GraphExploreRequest translate(TermsExplorationRequest request) {
        GraphExploreRequest exploreRequest = new GraphExploreRequest(request.getIndices());
        exploreRequest.setRouting(request.getRouting());
        exploreRequest.setTimeout(new TimeValue(request.getTimeout(), TimeUnit.MILLISECONDS));
        exploreRequest.setSteps(translate(request.getSteps()));
        return exploreRequest;
    }

    /**
     * translate steps
     * @param steps
     * @return
     */
    static List<Step> translate(List<TermsExplorationRequest.Steps> steps) {
        if (steps.isEmpty()) return Collections.emptyList();

        List<Step> stepList = new ArrayList<>();

        stepList.add(new Step(null, translateNodes(steps.get(0).getNodes()), buildQuery(steps.get(0))));

        for (int i = 1; i < steps.size(); i++) {
            stepList.add(new Step(stepList.get(i - 1),
                    translateNodes(steps.get(i).getNodes()),
                    buildQuery(steps.get(i))));
        }
        return stepList;
    }

    /**
     * translate vertex terms
     * @param nodes
     * @return
     */
    static List<VertexRequest> translateNodes(List<TermsExplorationRequest.Nodes> nodes) {
        return nodes.stream().map(node ->
                new VertexRequest(node.getFieldName(),
                        node.getSize(),
                        translate(node.getIncludes()),
                        node.getExcludes(),
                        node.getMinDocCount(),
                        node.getShardMinDocCount())).distinct().collect(Collectors.toList());
    }


    static Map<String, GraphExploreRequest.TermBoost> translate(Map<String, Float> includes) {
        Map<String, GraphExploreRequest.TermBoost> result = new HashMap<>();
        includes.forEach((key, value) -> result.put(key, new GraphExploreRequest.TermBoost(key, value)));
        return result;
    }

    /**
     * build the initial seed query
     *
     * @param step
     * @return
     */
    static QueryBuilder buildQuery(TermsExplorationRequest.Steps step) {
        if (step.getInitialTerms() == null)
            return null;

        TermsExplorationRequest.InitialTerms initialTerms = step.getInitialTerms();
        BoolQueryBuilder rootBool = QueryBuilders.boolQuery();
        switch (initialTerms.getQuantType()) {
            case some:
                initialTerms.getTerms().forEach(
                        term -> rootBool.should(termQuery(initialTerms.getField(), term)));
                break;
            case all:
                initialTerms.getTerms().forEach(
                        term -> rootBool.must(termQuery(initialTerms.getField(), term)));
                break;
        }
        return rootBool;
    }
}
