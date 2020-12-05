/*
 * Copyright Elasticsearch B.V. and/or licensed to Elasticsearch B.V. under one
 * or more contributor license agreements. Licensed under the Elastic License;
 * you may not use this file except in compliance with the Elastic License.
 */
package com.yangdb.fuse.executor.elasticsearch.terms;

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

import com.yangdb.fuse.executor.elasticsearch.terms.actions.AsyncGraphTermsTraversal;
import com.yangdb.fuse.executor.elasticsearch.terms.actions.GraphTermsTraversal;
import com.yangdb.fuse.executor.elasticsearch.terms.transport.GraphExploreRequest;
import com.yangdb.fuse.executor.elasticsearch.terms.transport.GraphExploreResponse;
import org.elasticsearch.action.ActionListener;
import org.elasticsearch.client.Client;

import javax.inject.Inject;

/**
 * Performs a series of elasticsearch queries and aggregations to explore
 * connected terms in a single index.
 *
 * This is a specialized similarity 'terms-graph' which is unstructured graph exploration based only on terms inside index.field
 * no structure / ontological considerations take place here in contrast to the regular ontology based graph query
 */
public class TermGraphExplorationDriver implements TermGraphExploration {

    private final Client client;

    @Inject
    public TermGraphExplorationDriver(Client client) {
        this.client = client;
    }

    /**
     * async activation
     * @param request
     * @param listener
     */
    @Override
    public void doExecute(GraphExploreRequest request, ActionListener<GraphExploreResponse> listener) {
        new AsyncGraphTermsTraversal(client, request, listener).start();
    }

    /**
     * sync call
     * @param request
     * @return
     */
    @Override
    public GraphExploreResponse execute(GraphExploreRequest request) {
        return new GraphTermsTraversal(client,request).start();
    }

}
