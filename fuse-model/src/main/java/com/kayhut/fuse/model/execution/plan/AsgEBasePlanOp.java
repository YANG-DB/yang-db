package com.kayhut.fuse.model.execution.plan;

import com.kayhut.fuse.model.asgQuery.AsgEBase;
import com.kayhut.fuse.model.query.EBase;

/**
 * Created by Roman on 30/04/2017.
 */
public abstract class AsgEBasePlanOp<T extends EBase> extends PlanOp implements AsgEBaseContainer<T> {
    //region Constructors
    public AsgEBasePlanOp() {}

    public AsgEBasePlanOp(AsgEBase<T> asgEBase) {
        this.asgEbase = asgEBase;
    }
    //endregion

    //region Override Methods
    @Override
    public String toString() {
        return this.getClass().getSimpleName() + "(" + this.asgEbase.toString() + ")";
    }
    //endregion

    //region Properties
    @Override
    public AsgEBase<T> getAsgEbase() {
        return asgEbase;
    }

    public void setAsgEbase(AsgEBase<T> value) {
        this.asgEbase = value;
    }
    //endregion

    //region Fields
    private AsgEBase<T> asgEbase;
    //endregion
}
