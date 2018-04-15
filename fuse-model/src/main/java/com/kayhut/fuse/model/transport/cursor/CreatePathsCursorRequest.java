package com.kayhut.fuse.model.transport.cursor;

import com.kayhut.fuse.model.transport.CreatePageRequest;

/**
 * Created by roman.margolis on 11/03/2018.
 */
public class CreatePathsCursorRequest extends CreateCursorRequest {
    //region Constructors
    public CreatePathsCursorRequest() {

    }

    public CreatePathsCursorRequest(CreatePageRequest createPageRequest) {
        super(createPageRequest);
    }

    public CreatePathsCursorRequest(Include include, CreatePageRequest createPageRequest) {
        super(include, createPageRequest);
    }
    //endregion
}
