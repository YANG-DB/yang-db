package com.kayhut.fuse.services;

import com.kayhut.fuse.model.execution.plan.Plan;
import com.kayhut.fuse.model.process.QueryResourceResult;
import com.kayhut.fuse.model.transport.QueryRequest;
import com.kayhut.fuse.model.transport.ContentResponse;

/**
 * Created by lior on 19/02/2017.
 */
public interface QueryController {

    ContentResponse<QueryResourceResult> query(QueryRequest request);
    ContentResponse<Plan> explain(String queryId);

}
