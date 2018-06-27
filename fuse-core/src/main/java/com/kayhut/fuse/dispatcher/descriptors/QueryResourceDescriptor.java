package com.kayhut.fuse.dispatcher.descriptors;

import com.kayhut.fuse.dispatcher.resource.QueryResource;
import com.kayhut.fuse.model.descriptors.Descriptor;

/**
 * Created by roman.margolis on 29/11/2017.
 */
public class QueryResourceDescriptor implements Descriptor<QueryResource> {
    //region Descriptor Implementation
    @Override
    public String describe(QueryResource item) {
        return String.format("Query{id: %s, name: %s, ont: %s}",
                item.getQueryMetadata().getId(),
                item.getQueryMetadata().getName(),
                item.getQuery().getOnt());
    }
    //endregion
}
