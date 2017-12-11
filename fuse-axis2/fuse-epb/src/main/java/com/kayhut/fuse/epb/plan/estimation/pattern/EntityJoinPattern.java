package com.kayhut.fuse.epb.plan.estimation.pattern;

import com.kayhut.fuse.model.execution.plan.entity.EntityJoinOp;

/**
 * Created by Roman on 29/06/2017.
 */
public class EntityJoinPattern extends Pattern {
    //region Constructors
    public EntityJoinPattern(EntityJoinOp entityJoinOp) {
        this.entityJoinOp = entityJoinOp;
    }
    //endregion

    //region Properties

    public EntityJoinOp getEntityJoinOp() {
        return entityJoinOp;
    }

    //endregion

    //region Fields
    private EntityJoinOp entityJoinOp;

    //endregion
}
