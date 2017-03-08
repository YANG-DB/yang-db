package com.kayhut.fuse.services;

import com.kayhut.fuse.model.execution.plan.Plan;
import com.kayhut.fuse.model.process.QueryResourceInfo;
import com.kayhut.fuse.model.transport.CreateQueryRequest;
import com.kayhut.fuse.model.transport.ContentResponse;

/**
 * Created by lior on 19/02/2017.
 */
public interface QueryController {
    ContentResponse<QueryResourceInfo> create(CreateQueryRequest request);
    ContentResponse<QueryResourceInfo> getInfo(String queryId);
    ContentResponse<Plan> explain(String queryId);
    ContentResponse<Boolean> delete(String queryId);

}
