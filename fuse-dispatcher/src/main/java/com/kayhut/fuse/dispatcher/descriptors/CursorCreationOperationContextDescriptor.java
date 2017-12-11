package com.kayhut.fuse.dispatcher.descriptors;

import com.kayhut.fuse.dispatcher.context.CursorCreationOperationContext;
import com.kayhut.fuse.model.descriptors.Descriptor;

/**
 * Created by roman.margolis on 29/11/2017.
 */
public class CursorCreationOperationContextDescriptor implements Descriptor<CursorCreationOperationContext> {
    //region Descriptor Implementation
    @Override
    public String describe(CursorCreationOperationContext context) {
        return context.getCursorId();
    }
    //endregion
}
