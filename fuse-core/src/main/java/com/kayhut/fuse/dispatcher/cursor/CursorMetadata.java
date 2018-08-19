package com.kayhut.fuse.dispatcher.cursor;

import com.kayhut.fuse.model.transport.cursor.CreateCursorRequest;

public class CursorMetadata<T> {
    private CreateCursorRequest.Include include;
    private String queryId;
    private String cursorType;
    private T cursorParams;
    private long ttl;
    private long creationTime;

    public CursorMetadata(String queryId, String cursorType, T cursorParams,CreateCursorRequest.Include include, long ttl, long creationTime) {
        this.queryId = queryId;
        this.cursorType = cursorType;
        this.cursorParams = cursorParams;
        this.include = include;
        this.ttl = ttl;
        this.creationTime = creationTime;
    }

    public CreateCursorRequest.Include getInclude() {
        return include;
    }

    public String getQueryId() {
        return queryId;
    }

    public String getCursorType() {
        return cursorType;
    }

    public T getCursorParams() {
        return cursorParams;
    }

    public long getTtl() {
        return ttl;
    }

    public long getCreationTime() {
        return creationTime;
    }
}
