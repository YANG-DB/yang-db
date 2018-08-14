package com.kayhut.fuse.dispatcher.resource.store;

import com.kayhut.fuse.dispatcher.resource.CursorResource;
import com.kayhut.fuse.dispatcher.resource.PageResource;
import com.kayhut.fuse.dispatcher.resource.QueryResource;

import java.util.Collection;
import java.util.Optional;

/**
 * Created by User on 06/03/2017.
 */
public interface ResourceStore {
    Collection<QueryResource> getQueryResources();
    Optional<QueryResource> getQueryResource(String queryId);
    Optional<CursorResource> getCursorResource(String queryId, String cursorId);
    Optional<PageResource> getPageResource(String queryId, String cursorId, String pageId);

    boolean addQueryResource(QueryResource queryResource);
    boolean deleteQueryResource(String queryId);

    boolean addCursorResource(String queryId, CursorResource cursorResource);
    boolean deleteCursorResource(String queryId, String cursorId);

    boolean addPageResource(String queryId, String cursorId, PageResource pageResource);
    boolean deletePageResource(String queryId, String cursorId, String pageId);
}
