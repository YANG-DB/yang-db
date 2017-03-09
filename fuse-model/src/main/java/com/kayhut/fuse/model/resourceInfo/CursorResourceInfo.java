package com.kayhut.fuse.model.resourceInfo;

import com.kayhut.fuse.model.transport.CreateCursorRequest;

/**
 * Created by User on 06/03/2017.
 */
public class CursorResourceInfo extends ResourceInfoBase {
    //region Constructors
    public CursorResourceInfo(String resourceUrl, CreateCursorRequest.CursorType cursorType, String pageStoreUrl) {
        super(resourceUrl);
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
    //endregion

    //region Fields
    private CreateCursorRequest.CursorType cursorType;
    private String pageStoreUrl;
    //endregion
}
