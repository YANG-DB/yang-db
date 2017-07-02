package com.kayhut.fuse.model.resourceInfo;

/**
 * Created by User on 09/03/2017.
 */
public class StoreResourceInfo extends ResourceInfoBase {
    //region Constructors
    public StoreResourceInfo() {}

    public StoreResourceInfo(String resourceUrl,String resourceId, Iterable<String> resourceUrls) {
        super(resourceUrl,resourceId);
        this.resourceUrls = resourceUrls;
    }
    //endregion

    //region Properties
    public Iterable<String> getResourceUrls() {
        return this.resourceUrls;
    }

    public void setResourceUrls(Iterable<String> resourceUrls) {
        this.resourceUrls = resourceUrls;
    }
    //endregion

    //region Fields
    private Iterable<String> resourceUrls;
    //endregion
}
