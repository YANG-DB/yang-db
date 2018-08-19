package com.kayhut.fuse.model.transport;

/**
 * Created by lior on 22/02/2017.
 */
public class CreatePageRequest {
    //region Constructors
    public CreatePageRequest() {}

    public CreatePageRequest(int pageSize) {
        this(pageSize, false);
    }

    public CreatePageRequest(int pageSize, boolean fetch) {
        this.pageSize = pageSize;
        this.fetch = fetch;
    }
    //endregion

    //region Properties
    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    public boolean isFetch() {
        return fetch;
    }

    public void setFetch(boolean fetch) {
        this.fetch = fetch;
    }
    //endregion

    @Override
    public String toString() {
        return "CreatePageRequest{" +
                "pageSize=" + pageSize +
                ", fetch=" + fetch +
                '}';
    }

    //region Fields
    private int pageSize = 1000;
    private boolean fetch;
    //endregion
}
