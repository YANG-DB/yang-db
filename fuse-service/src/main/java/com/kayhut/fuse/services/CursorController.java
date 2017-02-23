package com.kayhut.fuse.services;

import com.kayhut.fuse.model.transport.CursorFetchRequest;
import com.kayhut.fuse.model.transport.RequestType;
import com.kayhut.fuse.model.transport.Response;

/**
 * Created by lior on 22/02/2017.
 */
public interface CursorController {
    Response plan(String cursorId);
    Response fetch(String cursorId, CursorFetchRequest request);
    Response cancelFetch(String cursorId);
    Response delete(String cursorId);
}
