package com.kayhut.fuse.model.transport.cursor;

import com.kayhut.fuse.model.transport.CreatePageRequest;

public class CreateHierarchyFlattenCursorRequest extends CreateCursorRequest {
    public static final String CursorType = "hierarchyFlatten";

    //region Constructors
    public CreateHierarchyFlattenCursorRequest() {
        super(CursorType);
    }

    public CreateHierarchyFlattenCursorRequest(CreatePageRequest createPageRequest) {
        super(CursorType, createPageRequest);
    }

    public CreateHierarchyFlattenCursorRequest(Include include, CreatePageRequest createPageRequest) {
        super(CursorType, include, createPageRequest);
    }
    //endregion
}
