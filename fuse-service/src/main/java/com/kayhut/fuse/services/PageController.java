package com.kayhut.fuse.services;

import com.kayhut.fuse.model.process.PageResourceInfo;
import com.kayhut.fuse.model.results.QueryResult;
import com.kayhut.fuse.model.transport.ContentResponse;
import com.kayhut.fuse.model.transport.CreatePageRequest;

/**
 * Created by lior on 19/02/2017.
 */
public interface PageController {
    ContentResponse<PageResourceInfo> create(String queryId, String cursorId, CreatePageRequest createPageRequest);
    ContentResponse<Object> get(String queryId, String cursorId, String pageId);
    ContentResponse<Boolean> delete(String queryId, String cursorId, String pageId);
}
