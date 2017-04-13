package com.kayhut.fuse.model.resourceInfo;

/**
 * Created by User on 08/03/2017.
 */
public class PageResourceInfo extends ResourceInfoBase{
    //region Constructor
    public PageResourceInfo() {}

    public PageResourceInfo(String resourceUrl,String resourceId, int requestedPageSize, int actualPageSize, boolean isAvailable) {
        super(resourceUrl,resourceId);
        this.dataUrl = this.getResourceUrl() + "/data";
        this.requestedPageSize = requestedPageSize;
        this.actualPageSize = actualPageSize;
        this.isAvailable = isAvailable;
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

    public boolean isAvailable() {
        return this.isAvailable;
    }

    public void setDataUrl(String dataUrl) {
        this.dataUrl = dataUrl;
    }

    public void setRequestedPageSize(int requestedPageSize) {
        this.requestedPageSize = requestedPageSize;
    }

    public void setActualPageSize(int actualPageSize) {
        this.actualPageSize = actualPageSize;
    }

    public void setAvailable(boolean available) {
        isAvailable = available;
    }

    //endregion

    //region Fields
    private String dataUrl;
    private int requestedPageSize;
    private int actualPageSize;
    private boolean isAvailable;
    //endregion
}
