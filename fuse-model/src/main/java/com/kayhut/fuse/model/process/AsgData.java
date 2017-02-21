package com.kayhut.fuse.model.process;

/**
 * Created by lior on 20/02/2017.
 */
public class AsgData {
    private QueryMetadata metadata;

    public AsgData(QueryMetadata metadata) {
        this.metadata = metadata;
    }

    public QueryMetadata getMetadata() {
        return metadata;
    }
}
