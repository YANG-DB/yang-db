package com.kayhut.fuse.dispatcher.driver;

import com.kayhut.fuse.model.process.CursorResourceInfo;
import com.kayhut.fuse.model.transport.CreateCursorRequest;

import java.util.Optional;

/**
 * Created by lior on 21/02/2017.
 */
public interface CursorDispatcherDriver {
    Optional<CursorResourceInfo> create(String queryId, CreateCursorRequest.CursorType cursorType);
    Optional<CursorResourceInfo> getInfo(String queryId, int cursorId);
    Optional<Boolean> delete(String queryId, int cursorId);

    //Optional<CursorResourceInfo> fetch(String queryId, int cursorId, long fetchSize);
}
