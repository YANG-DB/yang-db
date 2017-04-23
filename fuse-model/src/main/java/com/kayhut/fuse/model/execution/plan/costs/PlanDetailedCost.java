package com.kayhut.fuse.model.execution.plan.costs;

/**
 * Created by Roman on 20/04/2017.
 */
public class PlanDetailedCost {
    //region properties

    public Cost getCost() {
        return cost;
    }

    public Iterable<Cost> getOpCosts() {
        return opCosts;
    }

    public void setCost(Cost cost) {
        this.cost = cost;
    }

    public void setOpCosts(Iterable<Cost> opCosts) {
        this.opCosts = opCosts;
    }

    //endregion

    //region Fields
    private Cost cost;
    private Iterable<Cost> opCosts;
    //endregion
}
