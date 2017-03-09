package com.kayhut.fuse.model.resourceInfo;

/**
 * Created by User on 09/03/2017.
 */
public class StoreResourceInfo extends ResourceInfoBase {
    //region Constructors
    public StoreResourceInfo(String resourceUrl, Iterable<String> resourceUrls) {
        super(resourceUrl);
        this.resourceUrls = resourceUrls;
    }
    //endregion

    //region Properties
    public Iterable<String> getResourceUrls() {
        return this.resourceUrls;
    }
    //endregion

    //region Fields
    private Iterable<String> resourceUrls;
    //endregion
}
