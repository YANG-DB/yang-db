package com.kayhut.fuse.model.resourceInfo;

/**
 * Created by User on 09/03/2017.
 */
public abstract class ResourceInfoBase {
    //region Constructor
    public ResourceInfoBase(String resourceUrl,String resourceId) {
        this.resourceUrl = resourceUrl;
        this.resourceId = resourceId;
    }
    //endregion

    //region properties
    public String getResourceUrl() {
        return this.resourceUrl;
    }

    public String getResourceId() {
        return resourceId;
    }
    //endregion

    //region Fields
    private String resourceUrl;
    private String resourceId;
    //endregion

}
