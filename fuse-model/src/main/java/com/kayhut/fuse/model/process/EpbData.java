package com.kayhut.fuse.model.process;

import com.kayhut.fuse.model.query.Query;
import com.kayhut.fuse.model.query.QueryMetadata;

/**
 * Created by lior on 20/02/2017.
 */
public class EpbData extends QueryCursorData {
    public EpbData(String id,QueryMetadata metadata, Query query,ResultMetadata resultMetadata) {
        super(id,metadata, query,resultMetadata);
    }
}
