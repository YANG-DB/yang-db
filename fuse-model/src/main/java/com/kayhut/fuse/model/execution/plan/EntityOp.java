package com.kayhut.fuse.model.execution.plan;

import com.kayhut.fuse.model.query.EBase;
import com.kayhut.fuse.model.query.EEntityBase;

/**
 * Created by User on 20/02/2017.
 */
public class EntityOp extends TaggedOp {
    //region Constructor
    public EntityOp() {

    }

    public EntityOp(EEntityBase entity) {
        super(entity.geteTag());
    }
    //endregion

    //properties
    public EEntityBase getEntity() {
        return this.entity;
    }

    public void setEntity(EEntityBase value) {
        this.entity = value;
    }
    //endregion

    //region Fields
    private EEntityBase entity;
    //endregion
}
