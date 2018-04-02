package com.kayhut.fuse.dispatcher.resource;

import com.kayhut.fuse.dispatcher.cursor.Cursor;
import com.kayhut.fuse.model.transport.cursor.CreateCursorRequest;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Created by User on 06/03/2017.
 */
public class CursorResource {
    //region Constructors
    public CursorResource(String cursorId, Cursor cursor, CreateCursorRequest cursorRequest) {
        this.cursorId = cursorId;
        this.pageResources = new HashMap<>();
        this.cursor = cursor;
        this.cursorRequest = cursorRequest;

        this.timeCreated = new Date(System.currentTimeMillis());
    }
    //endregion

    //region Public Methods
    public String getCursorId() {
        return this.cursorId;
    }

    public Date getTimeCreated() {
        return this.timeCreated;
    }

    public Iterable<PageResource> getPageResources() {
        return this.pageResources.values();
    }

    public Optional<PageResource> getPageResource(String pageId) {
        return Optional.ofNullable(this.pageResources.get(pageId));
    }

    public void addPageResource(String pageId, PageResource pageResource) {
        this.pageResources.put(pageId, pageResource);
    }

    public void deletePageResource(String pageId) {
        this.pageResources.remove(pageId);
    }

    public String getNextPageId() {
        return String.valueOf(this.pageSequence++);
    }

    public Cursor getCursor() {
        return cursor;
    }

    public CreateCursorRequest getCursorRequest() {
        return this.cursorRequest;
    }
    //endregion

    //region Fields
    private String cursorId;
    private CreateCursorRequest cursorRequest;
    private Cursor cursor;
    private Date timeCreated;

    private Map<String, PageResource> pageResources;
    private int pageSequence;
    //endregion
}
