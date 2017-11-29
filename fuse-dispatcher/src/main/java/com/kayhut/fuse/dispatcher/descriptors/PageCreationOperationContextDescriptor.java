package com.kayhut.fuse.dispatcher.descriptors;

import com.kayhut.fuse.dispatcher.context.PageCreationOperationContext;

/**
 * Created by roman.margolis on 29/11/2017.
 */
public class PageCreationOperationContextDescriptor implements Descriptor<PageCreationOperationContext>{
    //region Descriptor Implementation
    @Override
    public String describe(PageCreationOperationContext context) {
        return context.getPageId();
    }
    //endregion
}