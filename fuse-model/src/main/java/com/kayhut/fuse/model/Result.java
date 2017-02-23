package com.kayhut.fuse.model;

import com.kayhut.fuse.model.query.QueryMetadata;

/**
 * Created by lior on 19/02/2017.
 */
public final class Result {
    private QueryMetadata metadata;
    private Content content;

    public Result(QueryMetadata metadata, Content content) {
        this.metadata = metadata;
        this.content = content;
    }

    public QueryMetadata getMetadata() {
        return metadata;
    }

    public Content getContent() {
        return content;
    }
}
