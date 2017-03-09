package com.kayhut.fuse.model.resourceInfo;

/**
 * Created by User on 09/03/2017.
 */
public abstract class ResourceInfoBase {
    //region Constructor
    public ResourceInfoBase(String resourceUrl) {
        this.resourceUrl = resourceUrl;
    }
    //endregion

    //region properties
    public String getResourceUrl() {
        return this.resourceUrl;
    }
    //endregion

    //region Fields
    private String resourceUrl;
    //endregion
}
