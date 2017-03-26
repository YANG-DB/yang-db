package com.kayhut.fuse.model.execution.plan;

import com.kayhut.fuse.model.asgQuery.AsgEBase;
import com.kayhut.fuse.model.query.properties.EProp;

/**
 * Created by User on 20/02/2017.
 */
public class EntityFilterOp extends PlanOpBase {
    //region Constructors
    public EntityFilterOp() {

    }

    public EntityFilterOp(AsgEBase<EProp> eprop) {
        this.eprop = eprop;
    }
    //endregion

    //region Properties

    //endregion

    //region Methods
    @Override
    public int geteNum() {
        return this.eprop.geteNum();
    }
    //endregion

    //region Fields
    private AsgEBase<EProp> eprop;
    //endregion
}
