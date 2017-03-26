package com.kayhut.fuse.services;

import com.kayhut.fuse.model.execution.plan.Plan;
import com.kayhut.fuse.model.resourceInfo.QueryResourceInfo;
import com.kayhut.fuse.model.resourceInfo.StoreResourceInfo;
import com.kayhut.fuse.model.transport.ContentResponse;
import com.kayhut.fuse.model.transport.CreateQueryRequest;

/**
 * Created by lior on 19/02/2017.
 */
public interface QueryController {
    ContentResponse<QueryResourceInfo> create(CreateQueryRequest request);
    ContentResponse<StoreResourceInfo> getInfo();
    ContentResponse<QueryResourceInfo> getInfo(String queryId);
    ContentResponse<Plan> explain(String queryId);
    ContentResponse<Boolean> delete(String queryId);

}
