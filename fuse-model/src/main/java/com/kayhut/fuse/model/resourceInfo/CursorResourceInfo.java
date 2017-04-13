package com.kayhut.fuse.model.resourceInfo;

import com.kayhut.fuse.model.transport.CreateCursorRequest;

/**
 * Created by User on 06/03/2017.
 */
public class CursorResourceInfo extends ResourceInfoBase {
    //region Constructors
    public CursorResourceInfo() {}

    public CursorResourceInfo(String resourceUrl,String resourceId, CreateCursorRequest.CursorType cursorType, String pageStoreUrl) {
        super(resourceUrl,resourceId);
        this.pageStoreUrl = pageStoreUrl;
        this.cursorType = cursorType;
    }
    //endregion

    //region Properties
    public String getPageStoreUrl() {
        return this.pageStoreUrl;
    }

    public CreateCursorRequest.CursorType getCursorType() {
        return this.cursorType;
    }

    public void setCursorType(CreateCursorRequest.CursorType cursorType) {
        this.cursorType = cursorType;
    }

    public void setPageStoreUrl(String pageStoreUrl) {
        this.pageStoreUrl = pageStoreUrl;
    }

    //endregion

    //region Fields
    private CreateCursorRequest.CursorType cursorType;
    private String pageStoreUrl;
    //endregion
}
