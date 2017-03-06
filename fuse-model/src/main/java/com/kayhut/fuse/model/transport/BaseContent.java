package com.kayhut.fuse.model.transport;

/**
 * Created by lior on 19/02/2017.
 */
public abstract class BaseContent<T> implements Content<T> {
    //region Fields
    private String id;
    private T data;
    //endregion

    //region Constructors
    public BaseContent(String id, T data) {
        this.id = id;
        this.data = data;
    }
    //endregion

    //region properties
    @Override
    public String getId() {
        return id;
    }

    public T getData() {
        return this.data;
    }
    //endregion
}
