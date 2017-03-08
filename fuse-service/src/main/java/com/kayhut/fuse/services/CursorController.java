package com.kayhut.fuse.services;

import com.kayhut.fuse.model.process.CursorResourceInfo;
import com.kayhut.fuse.model.transport.ContentResponse;
import com.kayhut.fuse.model.transport.CreateCursorRequest;

/**
 * Created by lior on 22/02/2017.
 */
public interface CursorController {
    ContentResponse<CursorResourceInfo> create(String queryId, CreateCursorRequest createCursorRequest);
    ContentResponse<CursorResourceInfo> getInfo(String queryId, int cursorId);
    ContentResponse<Boolean> delete(String queryId, int cursorId);
}
