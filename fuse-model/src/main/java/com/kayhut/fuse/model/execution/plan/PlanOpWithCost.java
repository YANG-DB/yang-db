package com.kayhut.fuse.model.execution.plan;

/**
 * Created by moti on 3/27/2017.
 */
public class PlanOpWithCost<C> {
    //region Constructors
    public PlanOpWithCost(PlanOpBase opBase, C cost) {
        this.cost = cost;
        this.opBase = opBase;
    }
    //endregion

    //region Methods
    public C getCost() {
        return cost;
    }

    public PlanOpBase getOpBase() {
        return opBase;
    }
    //endregion

    //region Members
    private C cost;
    private PlanOpBase opBase;
    //endregion
}
