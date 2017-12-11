package com.kayhut.fuse.dispatcher.descriptors;

import com.google.inject.Inject;
import com.kayhut.fuse.dispatcher.context.QueryCreationOperationContext;
import com.kayhut.fuse.model.asgQuery.AsgQuery;
import com.kayhut.fuse.model.descriptors.Descriptor;

/**
 * Created by roman.margolis on 29/11/2017.
 */
public class QueryCreationOperationContextDescriptor implements Descriptor<QueryCreationOperationContext> {
    //region Constructors
    @Inject
    public QueryCreationOperationContextDescriptor(Descriptor<AsgQuery> asgQueryDescriptor) {
        this.asgQueryDescriptor = asgQueryDescriptor;
    }
    //endregion

    //region Desriptor Implementation
    @Override
    public String describe(QueryCreationOperationContext context) {
        String asgQueryDescription = "null";
        if(context.getAsgQuery() != null && this.asgQueryDescriptor != null) {
            asgQueryDescription = this.asgQueryDescriptor.describe(context.getAsgQuery());
        }

        return "{Query: {Name:" + context.getQuery().getName() + ",Ont:" + context.getQuery().getOnt()+"}," +
                "AsgQuery: {" + asgQueryDescription + "}}" ;
    }
    //endregion

    //region Fields
    private Descriptor<AsgQuery> asgQueryDescriptor;
    //endregion
}
