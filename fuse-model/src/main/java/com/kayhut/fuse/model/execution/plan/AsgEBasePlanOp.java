package com.kayhut.fuse.model.execution.plan;

import com.kayhut.fuse.model.asgQuery.AsgEBase;
import com.kayhut.fuse.model.query.EBase;
import com.kayhut.fuse.model.query.entity.EEntityBase;

/**
 * Created by Roman on 30/04/2017.
 */
public abstract class AsgEBasePlanOp<T extends EBase> extends PlanOpBase{
    //region Constructors
    public AsgEBasePlanOp() {}

    public AsgEBasePlanOp(AsgEBase<T> asgEBase) {
        this.asgEBase = asgEBase;
//        this.asgEBase = new AsgEBase<>(asgEBase.geteBase());
    }
    //endregion

    //region Override Methods
    @Override
    public String toString() {
        return this.getClass().getSimpleName() + "(" + this.getAsgEBase().toString() + ")";
    }

    @Override
    public int geteNum() {
        return this.asgEBase.geteNum();
    }
    //endregion

    //region Properties
    public AsgEBase<T> getAsgEBase() {
        return asgEBase;
    }

    public void setAsgEBase(AsgEBase<T> value) {
        this.asgEBase = value;
    }
    //endregion

    //region Fields
    private AsgEBase<T> asgEBase;
    //endregion
}
