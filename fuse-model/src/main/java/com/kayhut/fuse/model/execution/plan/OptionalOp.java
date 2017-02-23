package com.kayhut.fuse.model.execution.plan;

import java.util.Optional;

/**
 * Created by User on 23/02/2017.
 */
public class OptionalOp extends PlanOpBase {
    //region Constructors
    public OptionalOp() {

    }

    public OptionalOp(Plan plan) {
        this.plan = plan;
    }
    //endregion

    //region Properties
    public Plan getPlan() {
        return this.plan;
    }

    public void setPlan(Plan value) {
        this.plan = value;
    }
    //endregion

    //region Fields
    private Plan plan;
    //endregion
}
