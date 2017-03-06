package com.kayhut.fuse.model.transport;

import com.kayhut.fuse.model.results.QueryResult;

/**
 * Created by lior on 19/02/2017.
 */
public class GraphContent extends BaseContent<QueryResult> {
    public GraphContent(String id, QueryResult data) {
        super(id, data);
    }
}
