package com.kayhut.fuse.dispatcher.descriptors;

import com.kayhut.fuse.dispatcher.context.QueryValidationOperationContext;

/**
 * Created by roman.margolis on 29/11/2017.
 */
public class QueryValidationOperationContextDescriptor implements Descriptor<QueryValidationOperationContext>{
    //region Descriptor Implementation
    @Override
    public String describe(QueryValidationOperationContext context) {
        return "{Name:" + context.getQuery().getName() + ",Ont:" + context.getQuery().getOnt()+"}" ;
    }
    //endregion
}
