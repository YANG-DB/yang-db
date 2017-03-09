package com.kayhut.fuse.model.process;

import com.kayhut.fuse.model.transport.CreateCursorRequest;

/**
 * Created by User on 06/03/2017.
 */
public class CursorResourceInfo {
    //region Constructors
    public CursorResourceInfo(String resourceUrl, CreateCursorRequest.CursorType cursorType, String pageStoreUrl) {
        this.resourceUrl = resourceUrl;
        this.pageStoreUrl = pageStoreUrl;
        this.cursorType = cursorType;
    }
    //endregion

    //region Properties
    public String getResourceUrl() {
        return this.resourceUrl;
    }

    public String getPageStoreUrl() {
        return this.pageStoreUrl;
    }

    public CreateCursorRequest.CursorType getCursorType() {
        return this.cursorType;
    }
    //endregion

    //region Fields
    private String resourceUrl;
    private CreateCursorRequest.CursorType cursorType;
    private String pageStoreUrl;
    //endregion
}
