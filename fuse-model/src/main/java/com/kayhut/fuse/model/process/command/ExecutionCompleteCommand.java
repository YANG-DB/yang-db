package com.kayhut.fuse.model.process.command;

import com.kayhut.fuse.model.transport.Request;
import com.kayhut.fuse.model.transport.Response;

/**
 * Created by lior on 19/02/2017.
 */
public class ExecutionCompleteCommand {
    private Response response;

    public ExecutionCompleteCommand() {
    }

    public ExecutionCompleteCommand(Response data) {
        this.response = data;
    }

    public Response getResponse() {
        return response;
    }
}
