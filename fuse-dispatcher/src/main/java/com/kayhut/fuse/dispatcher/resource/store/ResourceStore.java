package com.kayhut.fuse.dispatcher.resource.store;

import com.kayhut.fuse.dispatcher.resource.CursorResource;
import com.kayhut.fuse.dispatcher.resource.PageResource;
import com.kayhut.fuse.dispatcher.resource.QueryResource;

import java.util.Optional;

/**
 * Created by User on 06/03/2017.
 */
public interface ResourceStore {
    Iterable<QueryResource> getQueryResources();
    Optional<QueryResource> getQueryResource(String queryId);
    Optional<CursorResource> getCursorResource(String queryId, String cursorId);
    Optional<PageResource> getPageResource(String queryId, String cursorId, String pageId);

    void addQueryResource(QueryResource queryResource);
    void deleteQueryResource(String queryId);

    void addCursorResource(String queryId, CursorResource cursorResource);
    void deleteCursorResource(String queryId, String cursorId);

    void addPageResource(String queryId, String cursorId, PageResource pageResource);
    void deletePageResource(String queryId, String cursorId, String pageId);
}
