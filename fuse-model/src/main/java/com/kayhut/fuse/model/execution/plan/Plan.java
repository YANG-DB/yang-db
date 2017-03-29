package com.kayhut.fuse.model.execution.plan;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by User on 22/02/2017.
 */
public class Plan<C> {
    //region Constructors

    public Plan() {}

    public Plan(List<PlanOpWithCost<C>> ops) {
        this.ops = ops;
    }
    //endregion

    //region Properties
    public List<PlanOpWithCost<C>> getOps() {
        return this.ops;
    }


    public void setOps(List<PlanOpWithCost<C>> ops) {
        this.ops = ops;
    }

    public boolean isPlanComplete() {
        return isPlanComplete;
    }

    public void setPlanComplete(boolean planComplete) {
        isPlanComplete = planComplete;
    }

    public C getCost() {
        return cost;
    }

    public void setCost(C cost) {
        this.cost = cost;
    }
//endregion

    //region Fields
    private List<PlanOpWithCost<C>> ops;
    private boolean isPlanComplete;
    private C cost;
    //endregion

}
