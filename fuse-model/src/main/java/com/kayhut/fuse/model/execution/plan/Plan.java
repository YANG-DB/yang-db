package com.kayhut.fuse.model.execution.plan;

import java.util.List;

/**
 * Created by User on 22/02/2017.
 */
public class Plan {
    //region Constructors
    public Plan() {

    }

    public Plan(List<PlanOpBase> ops) {
        this.ops = ops;
    }
    //endregion

    //region Properties
    public List<PlanOpBase> getOps() {
        return this.ops;
    }

    public void setOps(List<PlanOpBase> ops) {
        this.ops = ops;
    }
    //endregion

    //region Fields
    private List<PlanOpBase> ops;
    //endregion
}
