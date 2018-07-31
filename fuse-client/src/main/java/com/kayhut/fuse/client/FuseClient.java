package com.kayhut.fuse.client;

import com.kayhut.fuse.model.query.Query;
import com.kayhut.fuse.model.resourceInfo.CursorResourceInfo;
import com.kayhut.fuse.model.resourceInfo.FuseResourceInfo;
import com.kayhut.fuse.model.resourceInfo.PageResourceInfo;
import com.kayhut.fuse.model.resourceInfo.QueryResourceInfo;
import com.kayhut.fuse.model.results.QueryResultBase;
import com.kayhut.fuse.model.transport.cursor.CreateCursorRequest;

public interface FuseClient {
    FuseResourceInfo getFuseInfo();

    QueryResourceInfo postQuery(Query query);
    QueryResourceInfo getQuery(QueryResourceInfo queryResourceInfo);
    boolean deleteQuery(QueryResourceInfo queryResourceInfo);

    CursorResourceInfo postCursor(QueryResourceInfo queryResourceInfo, CreateCursorRequest cursorRequest);
    CursorResourceInfo getCursor(CursorResourceInfo cursorResourceInfo);
    boolean deleteCursor(CursorResourceInfo cursorResourceInfo);

    PageResourceInfo postPage(CursorResourceInfo cursorResourceInfo, int pageSize);
    PageResourceInfo getPage(PageResourceInfo pageResourceInfo);
    QueryResultBase getPageData(PageResourceInfo pageResourceInfo);
    boolean deletePage(PageResourceInfo pageResourceInfo);
}
