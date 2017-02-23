package com.kayhut.fuse.model.process;

import com.kayhut.fuse.model.query.Query;
import com.kayhut.fuse.model.query.QueryMetadata;

/**
 * Created by lior on 20/02/2017.
 */
public class QueryData extends ExecutionContext{

    public QueryData(QueryMetadata metadata, Query query) {
        super(metadata,query);
    }
}
