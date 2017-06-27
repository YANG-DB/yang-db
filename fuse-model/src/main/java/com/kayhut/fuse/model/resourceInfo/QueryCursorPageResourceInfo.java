package com.kayhut.fuse.model.resourceInfo;

/**
 * Created by Roman on 6/27/2017.
 */
public class QueryCursorPageResourceInfo extends QueryResourceInfo {
    //region Constructors
    public QueryCursorPageResourceInfo(
            String resourceUrl,
            String resourceId,
            String cursorStoreUrl,
            CursorResourceInfo cursorResourceInfo,
            PageResourceInfo pageResourceInfo) {
        super(resourceUrl, resourceId, cursorStoreUrl);

        this.cursorResourceInfo = cursorResourceInfo;
        this.pageResourceInfo = pageResourceInfo;
    }
    //endregion

    //region Properties
    public CursorResourceInfo getCursorResourceInfo() {
        return cursorResourceInfo;
    }

    public PageResourceInfo getPageResourceInfo() {
        return pageResourceInfo;
    }
    //endregion

    //region Fields
    private CursorResourceInfo cursorResourceInfo;
    private PageResourceInfo pageResourceInfo;
    //endregion
}
