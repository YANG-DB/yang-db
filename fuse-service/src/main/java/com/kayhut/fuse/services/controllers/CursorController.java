package com.kayhut.fuse.services.controllers;

import com.kayhut.fuse.model.resourceInfo.CursorResourceInfo;
import com.kayhut.fuse.model.resourceInfo.StoreResourceInfo;
import com.kayhut.fuse.model.transport.ContentResponse;
import com.kayhut.fuse.model.transport.CreateCursorRequest;

/**
 * Created by lior on 22/02/2017.
 */
public interface CursorController {
    ContentResponse<CursorResourceInfo> create(String queryId, CreateCursorRequest createCursorRequest);
    ContentResponse<StoreResourceInfo> getInfo(String queryId);
    ContentResponse<CursorResourceInfo> getInfo(String queryId, String cursorId);
    ContentResponse<Boolean> delete(String queryId, String cursorId);
}
