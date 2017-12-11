package com.kayhut.fuse.model.descriptors;

import com.kayhut.fuse.model.descriptors.Descriptor;

/**
 * Created by roman.margolis on 30/11/2017.
 */
public class ToStringDescriptor<Q> implements Descriptor<Q> {
    //region Descriptor Implementation
    @Override
    public String describe(Q item) {
        if (item == null) {
            return "NULL";
        }

        return item.toString();
    }
    //endregion
}
