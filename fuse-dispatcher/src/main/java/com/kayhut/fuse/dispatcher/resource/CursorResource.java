package com.kayhut.fuse.dispatcher.resource;

import com.kayhut.fuse.model.process.Cursor;
import com.kayhut.fuse.model.transport.ContentResponse;

import java.util.*;

/**
 * Created by User on 06/03/2017.
 */
public class CursorResource<T> {
    //region Constructors
    public CursorResource(Cursor cursor) {
        this.resultResources = new HashMap<>();
        this.cursor = cursor;
    }
    //endregion

    //region Public Methods
    public Optional<ContentResponse<T>> getResultResource(int key) {
        return Optional.ofNullable(this.resultResources.get(key));
    }

    public void addResultResource(int key, ContentResponse<T> resultResource) {
        this.resultResources.put(key, resultResource);
    }

    public int getNextSequence() {
        return this.sequence++;
    }

    public Cursor getCursor() {
        return cursor;
    }
    //endregion

    //region Fields
    private Cursor cursor;
    private Map<Integer, ContentResponse<T>> resultResources;
    private int sequence;
    //endregion
}
