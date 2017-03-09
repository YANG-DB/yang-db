package com.kayhut.fuse.model.process;

/**
 * resources
 * http://domain/fuse/query/:id
 * http://domain/fuse/query/:id/plan
 * http://domain/fuse/query/:id/cursor/:sequence
 * http://domain/fuse/query/:id/cursor/:sequence/result/:sequence
 */
public class QueryResourceInfo {
    //region Constructors
    public QueryResourceInfo(String resourceUrl, String cursorStoreUrl) {
        this.resourceUrl = resourceUrl;
        this.cursorStoreUrl = cursorStoreUrl;
    }
    //endregion

    //region Properties
    public String getResourceUrl() {
        return this.resourceUrl;
    }

    public String getCursorStoreUrl() {
        return cursorStoreUrl;
    }
    //endregion

    //region Fields
    private String resourceUrl;
    private String cursorStoreUrl;
    //endregion
}
