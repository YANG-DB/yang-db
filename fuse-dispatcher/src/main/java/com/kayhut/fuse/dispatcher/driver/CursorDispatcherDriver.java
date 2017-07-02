package com.kayhut.fuse.dispatcher.driver;

import com.kayhut.fuse.model.resourceInfo.CursorResourceInfo;
import com.kayhut.fuse.model.resourceInfo.StoreResourceInfo;
import com.kayhut.fuse.model.transport.CreateCursorRequest;

import java.util.Optional;

/**
 * Created by lior on 21/02/2017.
 */
public interface CursorDispatcherDriver {
    Optional<CursorResourceInfo> create(String queryId, CreateCursorRequest.CursorType cursorType);
    Optional<StoreResourceInfo> getInfo(String queryId);
    Optional<CursorResourceInfo> getInfo(String queryId, String cursorId);
    Optional<Boolean> delete(String queryId, String cursorId);
}
