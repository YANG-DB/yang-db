package com.kayhut.fuse.model.transport;

import com.kayhut.fuse.model.results.QueryResult;

/**
 * Created by lior on 19/02/2017.
 */
public class PathsContent extends BaseContent<QueryResult[]> {
    public PathsContent(String id, QueryResult[] data) {
        super(id, data);
    }
}
