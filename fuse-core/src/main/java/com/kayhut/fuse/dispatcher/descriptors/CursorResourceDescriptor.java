package com.kayhut.fuse.dispatcher.descriptors;

import com.kayhut.fuse.dispatcher.resource.CursorResource;
import com.kayhut.fuse.model.descriptors.Descriptor;

/**
 * Created by roman.margolis on 29/11/2017.
 */
public class CursorResourceDescriptor implements Descriptor<CursorResource> {
    //region Descriptor Implementation
    @Override
    public String describe(CursorResource item) {
        return String.format("Cursor{id: %s, type: %s}", item.getCursorId(), item.getCursorRequest());
    }
    //endregion
}
