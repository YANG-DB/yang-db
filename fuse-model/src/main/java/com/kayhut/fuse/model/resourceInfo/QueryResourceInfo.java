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
    public QueryResourceInfo() {}

    public QueryResourceInfo(String resourceUrl,String resourceId, String cursorStoreUrl) {
        super(resourceUrl,resourceId);
        this.cursorStoreUrl = cursorStoreUrl;
    }

    //endregion

    //region Properties
    public String getCursorStoreUrl() {
        return cursorStoreUrl;
    }

    public FuseError getError() {
        return error;
    }

    public void setCursorStoreUrl(String cursorStoreUrl) {
        this.cursorStoreUrl = cursorStoreUrl;
    }

    //endregion

    public QueryResourceInfo error(FuseError error) {
        QueryResourceInfo clone  = new QueryResourceInfo(this.getResourceUrl(), this.getResourceId(),cursorStoreUrl);
        clone.error = error;
        return clone ;
    }

    //region Fields
    private String cursorStoreUrl;

    private FuseError error;
    //endregion
}
