package com.kayhut.fuse.model.process;

/**
 * Created by User on 08/03/2017.
 */
public class PageResourceInfo {
    //region Constructor
    public PageResourceInfo(String resourceUrl, int requestedPageSize, int actualPageSize) {
        this.resourceUrl = resourceUrl;
        this.requestedPageSize = requestedPageSize;
        this.actualPageSize = actualPageSize;
    }
    //endregion

    //region Public Methods
    public PageResourceInfo withActualPageSize(int actualPageSize) {
        return new PageResourceInfo(this.resourceUrl, this.requestedPageSize, actualPageSize);
    }
    //endregion

    //region Properties
    public String getResourceUrl() {
        return this.resourceUrl;
    }

    public int getRequestedPageSize() {
        return this.requestedPageSize;
    }

    public int getActualPageSize() {
        return this.actualPageSize;
    }
    //endregion

    //region Fields
    private String resourceUrl;
    private int requestedPageSize;
    private int actualPageSize;
    //endregion
}
