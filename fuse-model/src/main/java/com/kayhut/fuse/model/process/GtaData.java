package com.kayhut.fuse.model.process;

/**
 * Created by lior on 20/02/2017.
 */
public class GtaData {
    private QueryMetadata metadata;

    public GtaData(QueryMetadata metadata) {
        this.metadata = metadata;
    }

    public QueryMetadata getMetadata() {
        return metadata;
    }
}
