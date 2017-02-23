package com.kayhut.fuse.model.execution.plan;

import com.kayhut.fuse.model.query.EProp;

/**
 * Created by User on 20/02/2017.
 */
public class EntityFilterOp extends PlanOpBase {
    //region Constructors
    public EntityFilterOp() {

    }

    public EntityFilterOp(EProp eprop) {
        this.eprop = eprop;
    }
    //endregion

    //region Properties
    public EProp getEprop() {
        return this.eprop;
    }

    public void setEprop(EProp eprop) {
        this.eprop = eprop;
    }
    //endregion

    //region Fields
    private EProp eprop;
    //endregion
}
