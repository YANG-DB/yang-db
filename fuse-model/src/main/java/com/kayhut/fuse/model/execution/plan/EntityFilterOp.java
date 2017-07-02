package com.kayhut.fuse.model.execution.plan;

import com.kayhut.fuse.model.asgQuery.AsgEBase;
import com.kayhut.fuse.model.query.entity.EEntityBase;
import com.kayhut.fuse.model.query.properties.EProp;
import com.kayhut.fuse.model.query.properties.EPropGroup;

/**
 * Created by User on 20/02/2017.
 */
public class EntityFilterOp extends AsgEBasePlanOp<EPropGroup> implements Filter{
    //region Constructors
    public EntityFilterOp() {

    }

    public EntityFilterOp(AsgEBase<EPropGroup> asgEBase) {
        super(asgEBase);
    }
    //endregion

    //region Properties
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
