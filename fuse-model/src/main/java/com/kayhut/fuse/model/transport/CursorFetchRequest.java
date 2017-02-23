package com.kayhut.fuse.model.transport;

/**
 * Created by lior on 22/02/2017.
 */
public class CursorFetchRequest {
    private long fetchSize;
    private String type;//Path Or Graph

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setFetchSize(long fetchSize) {
        this.fetchSize = fetchSize;
    }

    public long getFetchSize() {
        return fetchSize;
    }
}
