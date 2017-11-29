package com.kayhut.fuse.dispatcher.descriptors;

import com.kayhut.fuse.dispatcher.utils.ValidationContext;

/**
 * Created by roman.margolis on 29/11/2017.
 */
public class ValidationContextDescriptor implements Descriptor<ValidationContext> {
    //region Descriptor Implementation
    @Override
    public String describe(ValidationContext context) {
        return String.valueOf(context.valid());
    }
    //endregion
}
