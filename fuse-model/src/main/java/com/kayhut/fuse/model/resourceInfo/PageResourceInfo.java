package com.kayhut.fuse.model.resourceInfo;

/**
 * Created by User on 08/03/2017.
 */
public class PageResourceInfo extends ResourceInfoBase{
    //region Constructor
    public PageResourceInfo(String resourceUrl,String resourceId, int requestedPageSize, int actualPageSize) {
        super(resourceUrl,resourceId);
        this.dataUrl = this.getResourceUrl() + "/data";
        this.requestedPageSize = requestedPageSize;
        this.actualPageSize = actualPageSize;
    }
    //region Properties
    public String getDataUrl() {
        return this.dataUrl;
    }

    public int getRequestedPageSize() {
        return this.requestedPageSize;
    }

    public int getActualPageSize() {
        return this.actualPageSize;
    }
    //endregion

    //region Fields
    private String dataUrl;
    private int requestedPageSize;
    private int actualPageSize;
    //endregion
}
