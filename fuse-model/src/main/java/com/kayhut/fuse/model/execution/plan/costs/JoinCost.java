package com.kayhut.fuse.model.execution.plan.costs;

import com.kayhut.fuse.model.execution.plan.entity.EntityOp;

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

    @Override
    public void applyCountsUpdateFactor(double countUpdateFactor){
        super.applyCountsUpdateFactor(countUpdateFactor);
        this.leftBranchCost.getPlanStepCosts().forEach(op -> {
            if(op.getPlan().getOps().get(0) instanceof EntityOp)
               op.getCost().applyCountsUpdateFactor(countUpdateFactor);

        });

        this.rightBranchCost.getPlanStepCosts().forEach(op -> {
            if (op.getPlan().getOps().get(0) instanceof EntityOp)
                op.getCost().applyCountsUpdateFactor(countUpdateFactor);
        });
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
