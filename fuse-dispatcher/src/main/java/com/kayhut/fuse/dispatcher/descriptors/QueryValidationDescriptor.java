package com.kayhut.fuse.dispatcher.descriptors;

import com.kayhut.fuse.model.validation.QueryValidation;
import com.kayhut.fuse.model.descriptors.Descriptor;

/**
 * Created by roman.margolis on 29/11/2017.
 */
public class QueryValidationDescriptor implements Descriptor<QueryValidation> {
    //region Descriptor Implementation
    @Override
    public String describe(QueryValidation context) {
        return String.valueOf(context.valid());
    }
    //endregion
}
