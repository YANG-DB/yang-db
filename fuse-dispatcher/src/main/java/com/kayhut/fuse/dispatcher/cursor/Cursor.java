package com.kayhut.fuse.dispatcher.cursor;

import com.kayhut.fuse.model.results.QueryResult;

/**
 * Created by User on 06/03/2017.
 */
public interface Cursor {
    QueryResult getNextResults(int numResults);
}
