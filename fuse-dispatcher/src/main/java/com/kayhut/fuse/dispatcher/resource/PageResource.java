package com.kayhut.fuse.dispatcher.resource;

import java.util.Date;

/**
 * Created by User on 09/03/2017.
 */
public class PageResource<T> {
    //region Constructors
    public PageResource(String pageId, T data, int requestedSize) {
        this.pageId = pageId;
        this.timeCreated = new Date(System.currentTimeMillis());
        this.data = data;
        this.requestedSize = requestedSize;
    }
    //endregion

    //region Public Methods
    public PageResource<T> withActualSize(int actualSize) {
        PageResource<T> clone = this.cloneImpl();
        clone.actualSize = actualSize;
        return clone;
    }
    //endregion

    //region properties
    public String getPageId() {
        return this.pageId;
    }

    public Date getTimeCreated() {
        return this.timeCreated;
    }

    public T getData() {
        return this.data;
    }

    public int getRequestedSize() {
        return this.requestedSize;
    }

    public int getActualSize() {
        return this.actualSize;
    }
    //endregion

    //region Private Methods
    private PageResource<T> cloneImpl() {
        PageResource<T> clone = new PageResource<T>(this.pageId, this.data, this.requestedSize);
        clone.timeCreated = this.timeCreated;
        clone.actualSize = this.actualSize;
        return clone;
    }
    //endregion

    //region Fields
    private String pageId;
    private Date timeCreated;
    private T data;
    private int requestedSize;
    private int actualSize;
    //endregion
}
