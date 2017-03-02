package com.kayhut.fuse.model.execution.plan.costs;

import com.google.inject.Inject;
import com.google.inject.name.Named;

/**
 * Created by moti on 2/28/2017.
 */
public class SingleCost {
    //region fields
    private double cost;
    //endregion

    //region constructors
    @Inject
    public SingleCost(@Named("PlanCost") double cost) {
        this.cost = cost;
    }
    //endregion

    //region methods
    public double getCost() {
        return cost;
    }

    public void setCost(double cost) {
        this.cost = cost;
    }
    //endregion

}
