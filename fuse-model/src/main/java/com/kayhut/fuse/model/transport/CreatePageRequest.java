package com.kayhut.fuse.model.transport;

/**
 * Created by lior on 22/02/2017.
 */
public class CreatePageRequest {
    //region Constructors
    public CreatePageRequest() {

    }

    public CreatePageRequest(int pageSize) {
        this.pageSize = pageSize;
    }
    //endregion

    //region Properties
    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }
    //endregion

    //region Fields
    private int pageSize;
    //endregion
}
