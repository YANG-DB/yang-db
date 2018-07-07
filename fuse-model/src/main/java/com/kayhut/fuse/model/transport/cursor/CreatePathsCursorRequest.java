package com.kayhut.fuse.model.transport.cursor;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.kayhut.fuse.model.transport.CreatePageRequest;

/**
 * Created by roman.margolis on 11/03/2018.
 */
public class CreatePathsCursorRequest extends CreateCursorRequest {
    public static final String CursorType = "paths";

    //region Constructors
    public CreatePathsCursorRequest() {
        super(CursorType);
    }

    public CreatePathsCursorRequest(CreatePageRequest createPageRequest) {
        super(CursorType, createPageRequest);
    }

    public CreatePathsCursorRequest(Include include, CreatePageRequest createPageRequest) {
        super(CursorType, include, createPageRequest);
    }
    //endregion
}
