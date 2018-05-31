package com.kayhut.fuse.model.transport.cursor;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.kayhut.fuse.model.transport.CreatePageRequest;

import java.util.Collections;

/**
 * Created by roman.margolis on 11/03/2018.
 */
public class CreateGraphHierarchyCursorRequest extends CreateCursorRequest {
    public CreateGraphHierarchyCursorRequest() {
        this(Collections.emptyList(),TIMEOUT);

    }

    //region Constructors
    @Inject
    public CreateGraphHierarchyCursorRequest(@Named(defaultTimeout) long timeout) {
        this(Collections.emptyList(),timeout);
    }

    public CreateGraphHierarchyCursorRequest(Iterable<String> countTags,@Named(defaultTimeout) long timeout) {
        this(countTags, null,timeout);
    }

    public CreateGraphHierarchyCursorRequest(Iterable<String> countTags, CreatePageRequest createPageRequest,@Named(defaultTimeout) long timeout) {
        this(Include.all, countTags, createPageRequest,timeout);
    }

    public CreateGraphHierarchyCursorRequest(Include include, Iterable<String> countTags, CreatePageRequest createPageRequest,@Named(defaultTimeout) long timeout) {
        super(include, createPageRequest,timeout);
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
