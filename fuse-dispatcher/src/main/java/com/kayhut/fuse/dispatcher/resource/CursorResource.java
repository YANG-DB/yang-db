package com.kayhut.fuse.dispatcher.resource;

import com.kayhut.fuse.model.process.Cursor;
import com.kayhut.fuse.model.transport.CreateCursorRequest;

import java.util.*;

/**
 * Created by User on 06/03/2017.
 */
public class CursorResource<T> {
    //region Constructors
    public CursorResource(Cursor cursor, CreateCursorRequest.CursorType cursorType) {
        this.pageResources = new HashMap<>();
        this.cursor = cursor;
        this.cursorType = cursorType;
    }
    //endregion

    //region Public Methods
    public Optional<T> getPageResource(int pageId) {
        return Optional.ofNullable(this.pageResources.get(pageId));
    }

    public void addPageResource(int pageId, T pageResource) {
        this.pageResources.put(pageId, pageResource);
    }

    public void deletePageResource(int pageId) {
        this.pageResources.remove(pageId);
    }

    public int getNextPageSequence() {
        return this.pageSequence++;
    }

    public Cursor getCursor() {
        return cursor;
    }

    public CreateCursorRequest.CursorType getCursorType() {
        return this.cursorType;
    }
    //endregion

    //region Fields
    private CreateCursorRequest.CursorType cursorType;
    private Cursor cursor;
    private Map<Integer, T> pageResources;
    private int pageSequence;
    //endregion
}
