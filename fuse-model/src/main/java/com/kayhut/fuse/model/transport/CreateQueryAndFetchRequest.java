package com.kayhut.fuse.model.transport;

/**
 * Created by Roman on 6/26/2017.
 */
public class CreateQueryAndFetchRequest extends CreateQueryRequest{
    //region Properties
    public CreateCursorRequest getCreateCursorRequest() {
        return createCursorRequest;
    }

    public void setCreateCursorRequest(CreateCursorRequest createCursorRequest) {
        this.createCursorRequest = createCursorRequest;
    }

    public CreatePageRequest getCreatePageRequest() {
        return createPageRequest;
    }

    public void setCreatePageRequest(CreatePageRequest createPageRequest) {
        this.createPageRequest = createPageRequest;
    }
    //endregion

    //region Fields
    private CreateCursorRequest createCursorRequest;
    private CreatePageRequest createPageRequest;
    //endregion
}
