package com.kayhut.fuse.model.transport.cursor;

import com.kayhut.fuse.model.transport.CreatePageRequest;

public class CreateHierarchyFlattenCursorRequest extends CreateCursorRequest {
    //region Constructors
    public CreateHierarchyFlattenCursorRequest() {
        super();
    }

    public CreateHierarchyFlattenCursorRequest(CreatePageRequest createPageRequest) {
        super(createPageRequest);
    }

    public CreateHierarchyFlattenCursorRequest(Include include, CreatePageRequest createPageRequest) {
        super(include, createPageRequest);
    }
    //endregion
}
