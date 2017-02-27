package com.kayhut.fuse.dispatcher;

import com.kayhut.fuse.model.process.QueryCursorData;
import com.kayhut.fuse.model.process.QueryData;

/**
 * Created by lior on 21/02/2017.
 */
public interface QueryDispatcherDriver {
    QueryCursorData process(QueryData input);

}
