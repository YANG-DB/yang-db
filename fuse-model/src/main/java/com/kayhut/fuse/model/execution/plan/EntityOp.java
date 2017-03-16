package com.kayhut.fuse.model.execution.plan;

import com.kayhut.fuse.model.asgQuery.AsgEBase;
import com.kayhut.fuse.model.query.entity.EEntityBase;

import java.util.LinkedList;

/**
 * Created by User on 20/02/2017.
 */
public class EntityOp extends PlanOpBase {
    //region Constructor
    public EntityOp() {

    }

    public EntityOp(AsgEBase<EEntityBase> entity) {
        this.entity = entity;
    }
    //endregion

    //region Methods

    @Override
    public int geteNum() {
        return this.entity.geteNum();
    }

    //endregion

    //region properties

    public AsgEBase<EEntityBase> getEntity() {
        return entity;
    }

    public void setEntity(AsgEBase<EEntityBase> entity) {
        this.entity = entity;
    }

    //endregion

    //region Fields
    private AsgEBase<EEntityBase> entity;
    //endregion
}
