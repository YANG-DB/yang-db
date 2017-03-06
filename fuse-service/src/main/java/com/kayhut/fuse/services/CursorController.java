package com.kayhut.fuse.services;

import com.kayhut.fuse.model.transport.CursorFetchRequest;
import com.kayhut.fuse.model.transport.ContentResponse;

/**
 * Created by lior on 22/02/2017.
 */
public interface CursorController {
    ContentResponse fetch(String queryId, int cursorId, long fetchSize);
}
