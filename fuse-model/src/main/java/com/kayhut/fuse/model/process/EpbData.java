package com.kayhut.fuse.model.process;

/**
 * Created by lior on 20/02/2017.
 */
public class EpbData {
    private QueryMetadata metadata;

    public EpbData(QueryMetadata metadata) {
        this.metadata = metadata;
    }

    public QueryMetadata getMetadata() {
        return metadata;
    }
}
