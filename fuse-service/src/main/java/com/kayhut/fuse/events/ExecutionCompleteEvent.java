package com.kayhut.fuse.events;

import com.kayhut.fuse.model.transport.Request;
import com.kayhut.fuse.model.transport.Response;

/**
 * Created by lior on 19/02/2017.
 */
public class ExecutionCompleteEvent{
    private final Request request;
    private final Response response;

    public ExecutionCompleteEvent(Request request, Response data) {
        this.request = request;
        this.response = data;
    }

    public Request getRequest() {
        return request;
    }

    public Response getResponse() {
        return response;
    }
}
