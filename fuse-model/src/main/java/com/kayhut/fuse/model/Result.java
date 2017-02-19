package com.kayhut.fuse.model;

/**
 * Created by lior on 19/02/2017.
 */
public final class Result {
    private Metadata metadata;
    private Content content;

    public Result(Metadata metadata, Content content) {
        this.metadata = metadata;
        this.content = content;
    }

    public Metadata getMetadata() {
        return metadata;
    }

    public Content getContent() {
        return content;
    }
}
