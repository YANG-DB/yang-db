package com.kayhut.fuse.dispatcher.driver;

import com.kayhut.fuse.model.process.PageResourceInfo;
import com.kayhut.fuse.model.results.QueryResult;

import java.util.Optional;

/**
 * Created by User on 08/03/2017.
 */
public interface PageDispatcherDriver {
    Optional<PageResourceInfo> create(String queryId, int cursorId, int pageSize);
    Optional<Object> get(String queryId, int cursorId, int pageId);
    Optional<Boolean> delete(String queryId, int cursorId, int pageId);
}
