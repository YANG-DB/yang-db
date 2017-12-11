package com.kayhut.fuse.dispatcher.descriptors;

import com.kayhut.fuse.dispatcher.resource.PageResource;
import com.kayhut.fuse.model.descriptors.Descriptor;

/**
 * Created by roman.margolis on 29/11/2017.
 */
public class PageResourceDescriptor implements Descriptor<PageResource> {
    //region Descriptor Implementation
    @Override
    public String describe(PageResource item) {
        return String.format("Page{id: %s, requestedSize: %s}", item.getPageId(), item.getRequestedSize());
    }
    //endregion
}
