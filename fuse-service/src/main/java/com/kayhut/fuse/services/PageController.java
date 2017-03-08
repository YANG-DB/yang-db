package com.kayhut.fuse.services;

import com.kayhut.fuse.model.process.PageResourceInfo;
import com.kayhut.fuse.model.results.QueryResult;
import com.kayhut.fuse.model.transport.ContentResponse;
import com.kayhut.fuse.model.transport.CreatePageRequest;

/**
 * Created by lior on 19/02/2017.
 */
public interface PageController {
    ContentResponse<PageResourceInfo> create(String queryId, int cursorId, CreatePageRequest createPageRequest);
    ContentResponse<Object> get(String queryId, int cursorId, int resultId);
    ContentResponse<Boolean> delete(String queryId, int cursorId, int resultId);
}
