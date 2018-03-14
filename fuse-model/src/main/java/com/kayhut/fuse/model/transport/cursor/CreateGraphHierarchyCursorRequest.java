package com.kayhut.fuse.model.transport.cursor;

/**
 * Created by roman.margolis on 11/03/2018.
 */
public class CreateGraphHierarchyCursorRequest extends CreateCursorRequest {
    //region Constructors
    public CreateGraphHierarchyCursorRequest() {

    }

    public CreateGraphHierarchyCursorRequest(Iterable<String> countTags) {
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
