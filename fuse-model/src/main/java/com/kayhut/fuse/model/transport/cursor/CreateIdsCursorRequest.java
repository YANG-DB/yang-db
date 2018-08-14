package com.kayhut.fuse.model.transport.cursor;

import com.kayhut.fuse.model.transport.CreatePageRequest;

/**
 * Created by roman.margolis on 11/03/2018.
 */
public class CreateIdsCursorRequest extends CreateCursorRequest {
    public static final String CursorType = "ids";

    //region Constructors
    public CreateIdsCursorRequest() {
        super(CursorType);
    }

    public CreateIdsCursorRequest(CreatePageRequest createPageRequest) {
        super(CursorType, createPageRequest);
    }

    public CreateIdsCursorRequest(Include include, CreatePageRequest createPageRequest) {
        super(CursorType, include, createPageRequest);
    }
    //endregion
}
