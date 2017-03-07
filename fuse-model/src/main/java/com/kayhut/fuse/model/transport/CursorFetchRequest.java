package com.kayhut.fuse.model.transport;

/**
 * Created by lior on 22/02/2017.
 */
public class CursorFetchRequest {
    private long fetchSize;

    public CursorFetchRequest() {
    }

    public long getFetchSize() {
        return fetchSize;
    }

    public void setFetchSize(long fetchSize) {
        this.fetchSize = fetchSize;
    }
}
