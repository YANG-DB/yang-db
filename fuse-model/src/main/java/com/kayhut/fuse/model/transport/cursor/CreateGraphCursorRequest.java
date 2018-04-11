package com.kayhut.fuse.model.transport.cursor;

import com.kayhut.fuse.model.transport.CreatePageRequest;

/**
 * Created by roman.margolis on 11/03/2018.
 */
public class CreateGraphCursorRequest extends CreateCursorRequest {
    //region Constructors
    public CreateGraphCursorRequest() {

    }

    public CreateGraphCursorRequest(CreatePageRequest createPageRequest) {
        super(createPageRequest);
    }
    //endregion
}
