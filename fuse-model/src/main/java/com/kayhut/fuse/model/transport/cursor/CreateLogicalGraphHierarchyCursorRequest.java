package com.kayhut.fuse.model.transport.cursor;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.kayhut.fuse.model.transport.CreatePageRequest;

import java.util.Collections;

/**
 * Created by roman.margolis on 11/03/2018.
 */
public class CreateLogicalGraphHierarchyCursorRequest extends CreateCursorRequest {
    public CreateLogicalGraphHierarchyCursorRequest() {
        this(Collections.emptyList(),TIMEOUT);

    }

    //region Constructors
    @Inject
    public CreateLogicalGraphHierarchyCursorRequest(@Named(defaultTimeout) long timeout) {
        this(Collections.emptyList(),timeout);
    }

    public CreateLogicalGraphHierarchyCursorRequest(Iterable<String> countTags) {
        this(countTags, null,TIMEOUT);
    }

    public CreateLogicalGraphHierarchyCursorRequest(Iterable<String> countTags, @Named(defaultTimeout) long timeout) {
        this(countTags, null,timeout);
    }


    public CreateLogicalGraphHierarchyCursorRequest(Iterable<String> countTags, CreatePageRequest createPageRequest, @Named(defaultTimeout) long timeout) {
        this(Include.all, countTags, createPageRequest,timeout);
    }

    public CreateLogicalGraphHierarchyCursorRequest(Include include, Iterable<String> countTags, CreatePageRequest createPageRequest, @Named(defaultTimeout) long timeout) {
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
