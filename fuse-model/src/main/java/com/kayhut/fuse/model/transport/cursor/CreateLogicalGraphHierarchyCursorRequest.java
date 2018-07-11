package com.kayhut.fuse.model.transport.cursor;

import com.kayhut.fuse.model.transport.CreatePageRequest;

import java.util.Collections;

/**
 * Created by roman.margolis on 11/03/2018.
 */
public class CreateLogicalGraphHierarchyCursorRequest extends CreateCursorRequest {
    public static final String CursorType = "logicalGraph";
    public CreateLogicalGraphHierarchyCursorRequest() {
        this(Collections.emptyList());

    }

    //region Constructors

    public CreateLogicalGraphHierarchyCursorRequest(Iterable<String> countTags) {
        this(countTags, null);
    }


    public CreateLogicalGraphHierarchyCursorRequest(Iterable<String> countTags, CreatePageRequest createPageRequest) {
        this(Include.all, countTags, createPageRequest);
    }

    public CreateLogicalGraphHierarchyCursorRequest(Include include, Iterable<String> countTags, CreatePageRequest createPageRequest) {
        super( CursorType,include, createPageRequest);
        this.countTags = countTags;
    }
    //endregion

    //region Properties
    public Iterable<String> getCountTags() {
        return countTags;
    }

    public void setCountTags(Iterable<String> countTags) {
        this.countTags = countTags;
    }
    //endregion

    //region Fields
    private Iterable<String> countTags;
    //endregion
}
