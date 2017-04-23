package com.kayhut.fuse.model.execution.plan;

/**
 * Created by Roman on 20/04/2017.
 */
public class PlanWithCost<P, C> {
    //region Constructors
    public PlanWithCost(P plan, C cost) {
        this.plan = plan;
        this.cost = cost;
    }
    //endregion

    //region Properties

    public P getPlan() {
        return plan;
    }

    public C getCost() {
        return cost;
    }

    public void setPlan(P plan) {
        this.plan = plan;
    }

    public void setCost(C cost) {
        this.cost = cost;
    }

    //endregion

    //region Fields
    private P plan;
    private C cost;
    //endregion
}
