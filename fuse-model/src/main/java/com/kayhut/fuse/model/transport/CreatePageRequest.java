package com.kayhut.fuse.model.transport;

/**
 * Created by lior on 22/02/2017.
 */
public class CreatePageRequest {
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
