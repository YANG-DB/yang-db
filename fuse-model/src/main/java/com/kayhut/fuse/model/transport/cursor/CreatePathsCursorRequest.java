package com.kayhut.fuse.model.transport.cursor;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.kayhut.fuse.model.transport.CreatePageRequest;

/**
 * Created by roman.margolis on 11/03/2018.
 */
public class CreatePathsCursorRequest extends CreateCursorRequest {
    //region Constructors

    public CreatePathsCursorRequest() {
    }

    @Inject
    public CreatePathsCursorRequest(@Named(defaultTimeout) long timeout) {
        super(timeout);
    }

    public CreatePathsCursorRequest(CreatePageRequest createPageRequest) {
        super(createPageRequest);
    }

    public CreatePathsCursorRequest(Include include, CreatePageRequest createPageRequest) {
        super(include, createPageRequest);
    }
    //endregion
}
