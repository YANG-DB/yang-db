package com.kayhut.fuse.dispatcher.driver;

import com.kayhut.fuse.model.process.PageResourceInfo;
import com.kayhut.fuse.model.results.QueryResult;

import java.util.Optional;

/**
 * Created by User on 08/03/2017.
 */
public interface PageDispatcherDriver {
    Optional<PageResourceInfo> create(String queryId, String cursorId, int pageSize);
    Optional<Object> get(String queryId, String cursorId, String pageId);
    Optional<Boolean> delete(String queryId, String cursorId, String pageId);
}
