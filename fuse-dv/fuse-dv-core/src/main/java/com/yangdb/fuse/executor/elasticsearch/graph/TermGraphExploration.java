/*
 * Copyright Elasticsearch B.V. and/or licensed to Elasticsearch B.V. under one
 * or more contributor license agreements. Licensed under the Elastic License;
 * you may not use this file except in compliance with the Elastic License.
 */
package com.yangdb.fuse.executor.elasticsearch.graph;

import com.yangdb.fuse.executor.elasticsearch.graph.actions.AsyncGraphTermsTraversal;
import com.yangdb.fuse.executor.elasticsearch.graph.actions.GraphTermsTraversal;
import com.yangdb.fuse.executor.elasticsearch.graph.transport.GraphExploreRequest;
import com.yangdb.fuse.executor.elasticsearch.graph.transport.GraphExploreResponse;
import org.elasticsearch.action.ActionListener;
import org.elasticsearch.client.node.NodeClient;
import org.elasticsearch.common.inject.Inject;

/**
 * Performs a series of elasticsearch queries and aggregations to explore
 * connected terms in a single index.
 *
 * This is a specialized similarity 'terms-graph' which is unstructured graph exploration based only on terms inside index.field
 * no structure / ontological considerations take place here in contrast to the regular ontology based graph query
 */
public class TermGraphExploration  {

    private final NodeClient client;

    @Inject
    public TermGraphExploration(NodeClient client) {
        this.client = client;
    }

    /**
     * async activation
     * @param request
     * @param listener
     */
    public void doExecute( GraphExploreRequest request, ActionListener<GraphExploreResponse> listener) {
        new AsyncGraphTermsTraversal(client, request, listener).start();
    }

    /**
     * sync call
     * @param request
     * @return
     */
    protected GraphExploreResponse execute(GraphExploreRequest request) {
        return new GraphTermsTraversal(client,request).start();
    }

}
