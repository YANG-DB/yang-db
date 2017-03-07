package com.kayhut.fuse.dispatcher;

import com.kayhut.fuse.model.process.CursorResourceResult;

import java.util.Optional;

/**
 * Created by lior on 21/02/2017.
 */
public interface CursorDispatcherDriver {
    Optional<CursorResourceResult> fetch(String queryId, int cursorId, long fetchSize);
}
