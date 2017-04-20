package com.kayhut.fuse.model.execution.plan;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by User on 22/02/2017.
 */
public class Plan {
    //region Constructors
    private Plan() {}

    public Plan(List<PlanOpBase> ops) {
        this.ops = new ArrayList<>(ops);
    }

    public Plan(PlanOpBase...ops) {
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
