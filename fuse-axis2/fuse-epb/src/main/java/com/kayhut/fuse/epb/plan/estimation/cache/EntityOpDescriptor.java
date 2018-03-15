package com.kayhut.fuse.epb.plan.estimation.cache;

import com.kayhut.fuse.model.descriptors.Descriptor;
import com.kayhut.fuse.model.execution.plan.entity.EntityOp;
import com.kayhut.fuse.model.query.entity.ETyped;
import com.kayhut.fuse.model.query.entity.EUntyped;

/**
 * Created by roman.margolis on 15/03/2018.
 */
public class EntityOpDescriptor implements Descriptor<EntityOp> {
    @Override
    public String describe(EntityOp item) {
        return ETyped.class.isAssignableFrom(item.getAsgEbase().geteBase().getClass()) ?
                ((ETyped)item.getAsgEbase().geteBase()).geteType() :
                EUntyped.class.getSimpleName();
    }
}
