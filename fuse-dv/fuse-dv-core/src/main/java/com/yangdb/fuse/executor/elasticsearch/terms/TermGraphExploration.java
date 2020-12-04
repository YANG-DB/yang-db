package com.yangdb.fuse.executor.elasticsearch.terms;

import com.yangdb.fuse.executor.elasticsearch.terms.transport.GraphExploreRequest;
import com.yangdb.fuse.executor.elasticsearch.terms.transport.GraphExploreResponse;
import org.elasticsearch.action.ActionListener;

/**
 * term graph explorer driver
 */
public interface TermGraphExploration {
    /**
     *
     * @param request
     * @param listener
     */
    void doExecute(GraphExploreRequest request, ActionListener<GraphExploreResponse> listener);

    GraphExploreResponse execute(GraphExploreRequest request);
}
