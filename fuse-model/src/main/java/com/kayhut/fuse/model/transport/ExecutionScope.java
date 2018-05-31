package com.kayhut.fuse.model.transport;

public class ExecutionScope {

    public ExecutionScope() {
    }

    public ExecutionScope( long timeout) {
        this.timeout = timeout;
    }

    public long getTimeout() {
        return timeout;
    }

    private long timeout = 60 * 1000 * 3;

}
