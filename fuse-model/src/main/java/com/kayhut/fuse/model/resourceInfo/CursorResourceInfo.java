package com.kayhut.fuse.model.resourceInfo;

import com.kayhut.fuse.model.transport.cursor.CreateCursorRequest;

/**
 * Created by User on 06/03/2017.
 */
public class CursorResourceInfo extends ResourceInfoBase {
    //region Constructors
    public CursorResourceInfo() {}

    public CursorResourceInfo(String resourceUrl,String resourceId, CreateCursorRequest cursorRequest, String pageStoreUrl) {
        super(resourceUrl,resourceId);
        this.pageStoreUrl = pageStoreUrl;
        this.cursorRequest = cursorRequest;
    }
    //endregion

    //region Properties
    public String getPageStoreUrl() {
        return this.pageStoreUrl;
    }

    public void setPageStoreUrl(String pageStoreUrl) {
        this.pageStoreUrl = pageStoreUrl;
    }

    public CreateCursorRequest getCursorRequest() {
        return cursorRequest;
    }

    public void setCursorRequest(CreateCursorRequest cursorRequest) {
        this.cursorRequest = cursorRequest;
    }
    //endregion

    //region Fields
    private CreateCursorRequest cursorRequest;
    private String pageStoreUrl;
    //endregion
}
