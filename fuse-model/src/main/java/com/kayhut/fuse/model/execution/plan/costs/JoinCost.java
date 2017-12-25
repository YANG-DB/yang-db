package com.kayhut.fuse.model.execution.plan.costs;

import java.util.Stack;

/**
 * Created by mordechaic on 11/14/2017.
 */
public class JoinCost extends CountEstimatesCost {
    public JoinCost(double cost, double countEstimate, PlanDetailedCost leftBranchCost, PlanDetailedCost rightBranchCost) {
        super(cost, countEstimate);
        this.leftBranchCost = leftBranchCost;
        this.rightBranchCost = rightBranchCost;
    }

    public JoinCost(double cost, Stack<Double> countEstimates, PlanDetailedCost leftBranchCost, PlanDetailedCost rightBranchCost) {
        super(cost, countEstimates);
        this.leftBranchCost = leftBranchCost;
        this.rightBranchCost = rightBranchCost;
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        return new JoinCost(this.getCost(), this.getCountEstimates(), this.getLeftBranchCost(), this.getRightBranchCost());
    }

    public PlanDetailedCost getLeftBranchCost() {
        return leftBranchCost;
    }

    public PlanDetailedCost getRightBranchCost() {
        return rightBranchCost;
    }

    private PlanDetailedCost leftBranchCost;
    private PlanDetailedCost rightBranchCost;

}
