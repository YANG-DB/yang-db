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
        this.explainPlanUrl = resourceUrl +"/plan";
    }

    //endregion

    //region Properties
    public String getCursorStoreUrl() {
        return cursorStoreUrl;
    }

    public String getExplainPlanUrl() {
        return explainPlanUrl;
    }

    public FuseError getError() {
        return error;
    }
    //endregion

    public QueryResourceInfo error(FuseError error) {
        QueryResourceInfo clone  = new QueryResourceInfo(this.getResourceUrl(), this.getResourceId(), this.getCursorStoreUrl());
        clone.error = error;
        return clone ;
    }

    //region Fields
    private String cursorStoreUrl;
    private String explainPlanUrl;

    private FuseError error;

    //endregion
}
