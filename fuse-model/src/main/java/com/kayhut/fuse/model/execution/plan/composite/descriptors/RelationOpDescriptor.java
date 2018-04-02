package com.kayhut.fuse.model.execution.plan.composite.descriptors;

import com.kayhut.fuse.model.descriptors.Descriptor;
import com.kayhut.fuse.model.execution.plan.relation.RelationOp;

/**
 * Created by Roman on 3/13/2018.
 */
public class RelationOpDescriptor implements Descriptor<RelationOp> {
    //region Descriptor Implementation
    @Override
    public String describe(RelationOp item) {
        return String.format("%s(%s(%s))",
                item.getClass().getSimpleName(),
                item.getAsgEbase().geteBase().getrType(),
                item.getAsgEbase().geteBase().geteNum());
    }
    //endregion
}
