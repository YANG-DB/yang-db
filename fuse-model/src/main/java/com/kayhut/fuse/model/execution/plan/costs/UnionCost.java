package com.kayhut.fuse.model.execution.plan.costs;

import com.kayhut.fuse.model.execution.plan.entity.EntityOp;

import java.util.List;
import java.util.Stack;

public class UnionCost extends CountEstimatesCost {

    public UnionCost(double cost, double countEstimate, PlanDetailedCost commonPlanCost, List<PlanDetailedCost> branchesCosts) {
        super(cost, countEstimate);
        this.commonPlanCost = commonPlanCost;
        this.branchesCosts = branchesCosts;
    }

    public UnionCost(double cost, Stack<Double> countEstimates, PlanDetailedCost commonPlanCost, List<PlanDetailedCost> branchesCosts) {
        super(cost, countEstimates);
        this.commonPlanCost = commonPlanCost;
        this.branchesCosts = branchesCosts;
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        return new UnionCost(this.getCost(), (Stack<Double>) this.getCountEstimates().clone(), commonPlanCost,branchesCosts );
    }

    @Override
    public void applyCountsUpdateFactor(double countUpdateFactor){
        super.applyCountsUpdateFactor(countUpdateFactor);
        applyBranchCUF(countUpdateFactor, commonPlanCost);
        branchesCosts.forEach(cost -> applyBranchCUF(countUpdateFactor,cost));
    }


    private void applyBranchCUF(double countUpdateFactor, PlanDetailedCost branch){
        branch.getPlanStepCosts().forEach(op -> {
            if(op.getPlan().getOps().get(0) instanceof EntityOp){
                op.getCost().applyCountsUpdateFactor(countUpdateFactor);
            }
        });
    }

    public PlanDetailedCost getCommonPlanCost() {
        return commonPlanCost;
    }

    public List<PlanDetailedCost> getBranchesCosts() {
        return branchesCosts;
    }

    private PlanDetailedCost commonPlanCost;
    private List<PlanDetailedCost> branchesCosts;

}
