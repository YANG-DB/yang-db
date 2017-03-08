package com.kayhut.fuse.dispatcher.urlSupplier;

import java.util.Optional;

/**
 * Created by User on 08/03/2017.
 */
public class ResourceStoreUrlSupplier extends ResourceUrlSupplier {
    public enum Store {
        Query,
        Cursor,
        Page,
    }

    //region Constructors
    public ResourceStoreUrlSupplier(String baseUrl, Store store) {
        super(baseUrl);
        this.store = store;
    }
    //endregion

    //region UrlSupplierBase Implementation
    @Override
    public String get() {
        switch (store) {
            case Query: return baseUrl + "/query";

            case Cursor:
                if (!this.queryId.isPresent()) {
                    return null;
                }
                return baseUrl + "/query/" + this.queryId.get() + "/cursor";

            case Page:
                if (!this.queryId.isPresent() || !this.cursorId.isPresent()) {
                    return null;
                }
                return baseUrl + "/query/" + this.queryId.get() + "/cursor/" + this.cursorId.get() + "/page";

            default: return null;
        }
    }
    //endregion

    //region Protected Methods
    protected ResourceUrlSupplier cloneImpl() {
        ResourceUrlSupplier clone = new ResourceStoreUrlSupplier(this.baseUrl, this.store);
        clone.queryId = this.queryId;
        clone.cursorId = this.cursorId;
        clone.pageId = this.pageId;
        return clone;
    }
    //endregion

    //region Fields
    protected Store store;
    //endregion
}
