package com.kayhut.fuse.model.execution.plan;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by Roman on 24/04/2017.
 */
public abstract class CompositePlanOpBase extends PlanOpBase {
    //region Constructors
    private CompositePlanOpBase() {}

    public CompositePlanOpBase(List<PlanOpBase> ops) {
        this.ops = new ArrayList<>(ops);
    }

    public CompositePlanOpBase(PlanOpBase...ops) {
        this.ops = new ArrayList<>(Arrays.asList(ops));
    }
    //endregion

    //region Properties
    public Plan withOp(PlanOpBase op) {
        Plan newPlan = new Plan(this.getOps());
        newPlan.getOps().add(op);
        return newPlan;
    }

    public List<PlanOpBase> getOps() {
        return this.ops;
    }
    //endregion

    //region Fields
    private List<PlanOpBase> ops;
    //endregion
}
