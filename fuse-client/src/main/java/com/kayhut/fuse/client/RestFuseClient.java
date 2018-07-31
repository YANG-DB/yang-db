package com.kayhut.fuse.client;

import com.kayhut.fuse.model.query.Query;
import com.kayhut.fuse.model.resourceInfo.CursorResourceInfo;
import com.kayhut.fuse.model.resourceInfo.FuseResourceInfo;
import com.kayhut.fuse.model.resourceInfo.PageResourceInfo;
import com.kayhut.fuse.model.resourceInfo.QueryResourceInfo;
import com.kayhut.fuse.model.results.QueryResultBase;
import com.kayhut.fuse.model.transport.cursor.CreateCursorRequest;
import org.eclipse.jetty.client.HttpClient;

public class RestFuseClient implements FuseClient, AutoCloseable {
    //region Constructors
    public RestFuseClient(String fuseUrl) throws Exception {
        this.httpClient = new HttpClient();
        this.httpClient.start();

        this.fuseResourceInfo = this.getFuseInfo();
    }
    //endregion

    //region AutoClosable Implementation
    @Override
    public void close() throws Exception {
        this.httpClient.stop();
    }
    //endregion

    //region FuseClient Implementation
    @Override
    public FuseResourceInfo getFuseInfo() {
        return null;
    }

    @Override
    public QueryResourceInfo postQuery(Query query) {
        return null;
    }

    @Override
    public QueryResourceInfo getQuery(QueryResourceInfo queryResourceInfo) {
        return null;
    }

    @Override
    public boolean deleteQuery(QueryResourceInfo queryResourceInfo) {
        return false;
    }

    @Override
    public CursorResourceInfo postCursor(QueryResourceInfo queryResourceInfo, CreateCursorRequest cursorRequest) {
        return null;
    }

    @Override
    public CursorResourceInfo getCursor(CursorResourceInfo cursorResourceInfo) {
        return null;
    }

    @Override
    public boolean deleteCursor(CursorResourceInfo cursorResourceInfo) {
        return false;
    }

    @Override
    public PageResourceInfo postPage(CursorResourceInfo cursorResourceInfo, int pageSize) {
        return null;
    }

    @Override
    public PageResourceInfo getPage(PageResourceInfo pageResourceInfo) {
        return null;
    }

    @Override
    public QueryResultBase getPageData(PageResourceInfo pageResourceInfo) {
        return null;
    }

    @Override
    public boolean deletePage(PageResourceInfo pageResourceInfo) {
        return false;
    }
    //endregion

    //region Fields
    private String fuseUrl;
    private HttpClient httpClient;
    private FuseResourceInfo fuseResourceInfo;
    //endregion
}
