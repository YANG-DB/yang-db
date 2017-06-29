package com.kayhut.fuse.model.transport;

import com.kayhut.fuse.model.query.Query;

/**
 * Created by Roman on 6/26/2017.
 */
public class CreateQueryAndFetchRequest extends CreateQueryRequest{
    //region Constructors
    public CreateQueryAndFetchRequest() {

    }

    public CreateQueryAndFetchRequest(String id, String name, Query query) {
        super(id, name, query);
    }

    public CreateQueryAndFetchRequest(
            String id,
            String name,
            Query query,
            CreateCursorRequest createCursorRequest,
            CreatePageRequest createPageRequest) {
        super(id, name, query);
        this.createCursorRequest = createCursorRequest;
        this.createPageRequest = createPageRequest;
    }

    public CreateQueryAndFetchRequest(String id, String name, Query query, boolean verbose) {
        super(id, name, query, verbose);
    }

    public CreateQueryAndFetchRequest(
            String id,
            String name,
            Query query,
            boolean verbose,
            CreateCursorRequest createCursorRequest,
            CreatePageRequest createPageRequest) {
        super(id, name, query, verbose);
        this.createCursorRequest = createCursorRequest;
        this.createPageRequest = createPageRequest;
    }
    //endregion

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
