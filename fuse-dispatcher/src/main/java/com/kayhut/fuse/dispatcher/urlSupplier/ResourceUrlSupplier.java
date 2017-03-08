package com.kayhut.fuse.dispatcher.urlSupplier;

import java.util.Optional;

/**
 * Created by User on 08/03/2017.
 */
public class ResourceUrlSupplier extends UrlSupplierBase {
    //region Constructors
    public ResourceUrlSupplier(String baseUrl) {
        super(baseUrl);
        this.queryId = Optional.empty();
        this.cursorId = Optional.empty();
        this.pageId = Optional.empty();
    }
    //endregion

    //region Public Methods
    public ResourceUrlSupplier queryId(String queryId) {
        ResourceUrlSupplier clone = cloneImpl();
        clone.queryId = Optional.of(queryId);
        return clone;
    }

    public ResourceUrlSupplier cursorId(int cursorId) {
        ResourceUrlSupplier clone = cloneImpl();
        clone.cursorId = Optional.of(cursorId);
        return clone;
    }

    public ResourceUrlSupplier pageId(int pageId) {
        ResourceUrlSupplier clone = cloneImpl();
        clone.pageId = Optional.of(pageId);
        return clone;
    }
    //endregion

    //region UrlSupplierBase Implementation
    @Override
    public String get() {
        if (!this.queryId.isPresent()) {
            return null;
        }

        if (!this.cursorId.isPresent()) {
            return this.baseUrl + "/query/" + this.queryId.get();
        }

        if (!this.pageId.isPresent()) {
            return this.baseUrl + "/query/" + this.queryId.get() + "/cursor/" + this.cursorId.get();
        }

        return this.baseUrl + "/query/" + this.queryId.get() + "/cursor/" + this.cursorId.get() + "/page/" + this.pageId.get();
    }
    //endregion

    //region Protected Methods
    protected ResourceUrlSupplier cloneImpl() {
        ResourceUrlSupplier clone = new ResourceUrlSupplier(this.baseUrl);
        clone.queryId = this.queryId;
        clone.cursorId = this.cursorId;
        clone.pageId = this.pageId;
        return clone;
    }
    //endregion

    //region Fields
    protected Optional<String> queryId;
    protected Optional<Integer> cursorId;
    protected Optional<Integer> pageId;
    //endregion
}
