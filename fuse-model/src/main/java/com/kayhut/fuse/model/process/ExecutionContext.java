package com.kayhut.fuse.model.process;

import com.kayhut.fuse.model.query.Query;
import com.kayhut.fuse.model.query.QueryMetadata;

/**
 * Created by lior on 22/02/2017.
 */
public abstract class ExecutionContext {
    private QueryMetadata queryMetadata;
    private Query query;

    public ExecutionContext(QueryMetadata queryMetadata, Query query) {
        this.queryMetadata = queryMetadata;
        this.query = query;
    }

    public QueryMetadata getQueryMetadata() {
        return queryMetadata;
    }

    public Query getQuery() {
        return query;
    }
}
