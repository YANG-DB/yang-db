package com.kayhut.fuse.dispatcher.cursor;

import com.kayhut.fuse.model.results.QueryResultBase;

/**
 * Created by User on 06/03/2017.
 */
public interface Cursor {
    QueryResultBase getNextResults(int numResults);
}
