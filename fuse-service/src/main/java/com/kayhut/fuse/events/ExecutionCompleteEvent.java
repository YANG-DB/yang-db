package com.kayhut.fuse.events;

import com.kayhut.fuse.model.Content;
import com.kayhut.fuse.model.transport.Request;

/**
 * Created by lior on 19/02/2017.
 */
public class ExecutionCompleteEvent{
    private final Request request;
    private final Content data;

    public ExecutionCompleteEvent(Request request, Content data) {
        this.request = request;
        this.data = data;
    }

    public Request getRequest() {
        return request;
    }

    public Content getData() {
        return data;
    }
}
