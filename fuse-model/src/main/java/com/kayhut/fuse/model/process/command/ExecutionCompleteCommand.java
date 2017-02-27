package com.kayhut.fuse.model.process.command;

import com.kayhut.fuse.model.transport.ContentResponse;

/**
 * Created by lior on 19/02/2017.
 */
public class ExecutionCompleteCommand {
    private ContentResponse response;

    public ExecutionCompleteCommand() {
    }

    public ExecutionCompleteCommand(ContentResponse data) {
        this.response = data;
    }

    public ContentResponse getResponse() {
        return response;
    }
}
