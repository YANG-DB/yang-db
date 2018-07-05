package com.kayhut.fuse.model.transport.cursor;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.kayhut.fuse.model.transport.CreatePageRequest;

import java.util.Collections;

/**
 * Created by roman.margolis on 11/03/2018.
 */
public class CreateGraphHierarchyCursorRequest extends CreateCursorRequest {
    //region Constructors
    public CreateGraphHierarchyCursorRequest(Iterable<String> countTags) {
        this(countTags, null);
    }

    public CreateGraphHierarchyCursorRequest(Iterable<String> countTags, CreatePageRequest createPageRequest) {
        this(Include.all, countTags, createPageRequest);
    }

    public CreateGraphHierarchyCursorRequest(Include include, Iterable<String> countTags, CreatePageRequest createPageRequest) {
        super(include, createPageRequest);
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
