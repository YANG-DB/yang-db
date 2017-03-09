package com.kayhut.fuse.model.resourceInfo;

/**
 * resources
 * http://domain/fuse/query/:id
 * http://domain/fuse/query/:id/plan
 * http://domain/fuse/query/:id/cursor/:sequence
 * http://domain/fuse/query/:id/cursor/:sequence/result/:sequence
 */
public class QueryResourceInfo extends ResourceInfoBase{
    //region Constructors
    public QueryResourceInfo(String resourceUrl, String cursorStoreUrl) {
        super(resourceUrl);
        this.cursorStoreUrl = cursorStoreUrl;
    }
    //endregion

    //region Properties
    public String getCursorStoreUrl() {
        return cursorStoreUrl;
    }
    //endregion

    //region Fields
    private String cursorStoreUrl;
    //endregion
}
