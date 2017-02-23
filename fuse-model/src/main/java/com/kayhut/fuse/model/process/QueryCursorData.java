package com.kayhut.fuse.model.process;

import com.kayhut.fuse.model.query.Query;
import com.kayhut.fuse.model.query.QueryMetadata;
import com.kayhut.fuse.model.results.ResultMetadata;

/**
 * Created by lior on 23/02/2017.
 */
public class QueryCursorData extends ExecutionContext{
    private String id;
    private ResultMetadata resultMetadata;

    public QueryCursorData(String id,QueryMetadata queryMetadata, Query query, ResultMetadata resultMetadata) {
        super(queryMetadata, query);
        this.id = id;
        this.resultMetadata = resultMetadata;
    }

    public ResultMetadata getResultMetadata() {
        return resultMetadata;
    }

    public String getId() {
        return id;
    }
}
