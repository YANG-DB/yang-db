package com.kayhut.fuse.dispatcher.urlSupplier;

/**
 * Created by User on 08/03/2017.
 */
public class DefaultAppUrlSupplier implements AppUrlSupplier {
    //region Constructors
    public DefaultAppUrlSupplier(String baseUrl) {
        this.baseUrl = baseUrl;
    }
    //endregion

    //region AppUrlSupplier Implementation
    @Override
    public String resourceUrl(String queryId) {
        return new ResourceUrlSupplier(this.baseUrl).queryId(queryId).get();
    }

    @Override
    public String resourceUrl(String queryId, int cursorId) {
        return new ResourceUrlSupplier(this.baseUrl).queryId(queryId).cursorId(cursorId).get();
    }

    @Override
    public String resourceUrl(String queryId, int cursorId, int pageId) {
        return new ResourceUrlSupplier(this.baseUrl).queryId(queryId).cursorId(cursorId).pageId(pageId).get();
    }

    @Override
    public String queryStoreUrl() {
        return new ResourceStoreUrlSupplier(this.baseUrl, ResourceStoreUrlSupplier.Store.Query).get();
    }

    @Override
    public String cursorStoreUrl(String queryId) {
        return new ResourceStoreUrlSupplier(this.baseUrl, ResourceStoreUrlSupplier.Store.Cursor).queryId(queryId).get();
    }

    @Override
    public String pageStoreUrl(String queryId, int cursorId) {
        return new ResourceStoreUrlSupplier(this.baseUrl, ResourceStoreUrlSupplier.Store.Page).queryId(queryId).cursorId(cursorId).get();
    }
    //endregion

    //region Fields
    protected String baseUrl;
    //endregion
}
