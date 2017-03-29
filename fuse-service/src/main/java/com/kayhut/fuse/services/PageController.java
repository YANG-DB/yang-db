package com.kayhut.fuse.services;

import com.kayhut.fuse.model.resourceInfo.PageResourceInfo;
import com.kayhut.fuse.model.resourceInfo.StoreResourceInfo;
import com.kayhut.fuse.model.transport.ContentResponse;
import com.kayhut.fuse.model.transport.CreatePageRequest;

/**
 * Created by lior on 19/02/2017.
 */
public interface PageController {
    ContentResponse<PageResourceInfo> create(String queryId, String cursorId, CreatePageRequest createPageRequest);
    ContentResponse<StoreResourceInfo> getInfo(String queryId, String cursorId);
    ContentResponse<PageResourceInfo> getInfo(String queryId, String cursorId, String pageId);
    ContentResponse<Object> getData(String queryId, String cursorId, String pageId);
}
