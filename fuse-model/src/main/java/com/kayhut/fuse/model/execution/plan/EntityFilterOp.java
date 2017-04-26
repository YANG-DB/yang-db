package com.kayhut.fuse.model.execution.plan;

import com.kayhut.fuse.model.asgQuery.AsgEBase;
import com.kayhut.fuse.model.query.entity.EEntityBase;
import com.kayhut.fuse.model.query.properties.EProp;

/**
 * Created by User on 20/02/2017.
 */
public class EntityFilterOp extends PlanOpBase implements Filter{
    //region Constructors
    public EntityFilterOp() {

    }

    public EntityFilterOp(AsgEBase<EProp> eprop) {
        this.eprop = eprop;
    }
    //endregion

    //region Properties

    public AsgEBase<EProp> getEprop() {
        return eprop;
    }

    //endregion

    //region Methods
    @Override
    public int geteNum() {
        return this.eprop.geteNum();
    }


    public AsgEBase<EEntityBase> getEntity() {
        return entity;
    }

    public void setEntity(AsgEBase<EEntityBase> entity) {
        this.entity = entity;
    }
    //endregion

    //region Fields
    private AsgEBase<EProp> eprop;
    //region Fields
    private AsgEBase<EEntityBase> entity;

    //endregion
}
