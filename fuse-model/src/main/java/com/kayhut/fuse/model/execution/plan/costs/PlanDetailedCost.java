package com.kayhut.fuse.model.execution.plan.costs;

import com.kayhut.fuse.model.execution.plan.*;
import com.kayhut.fuse.model.execution.plan.composite.CompositePlanOp;
import com.kayhut.fuse.model.execution.plan.composite.Plan;
import com.kayhut.fuse.model.execution.plan.entity.EntityJoinOp;
import javaslang.collection.Stream;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Created by Roman on 20/04/2017.
 */
public class PlanDetailedCost implements Cost, Cloneable {
    public PlanDetailedCost() {
    }

    public PlanDetailedCost(DoubleCost globalCost, Iterable<PlanWithCost<Plan, CountEstimatesCost>> planStepCosts) {
        this.globalCost = globalCost;
        this.planStepCosts = planStepCosts;
    }

    public PlanDetailedCost(PlanDetailedCost previousCost) {
        //todo implement clone
        this(previousCost.globalCost, previousCost.planStepCosts);
    }
    //region properties

    public DoubleCost getGlobalCost() {
        return globalCost;
    }

    public List<PlanOp> getPlanOps() {
        if (planStepCosts != null) {
            return Stream.ofAll(planStepCosts).flatMap(pc -> Stream.ofAll(pc.getPlan().getOps())).toJavaList();
        }

        return Collections.emptyList();
    }

    public Iterable<PlanWithCost<Plan, CountEstimatesCost>> getPlanStepCosts() {
        return planStepCosts;
    }

    public Optional<PlanWithCost<Plan, CountEstimatesCost>> getPlanStepCost(PlanOp planOp) {
        Optional<PlanWithCost<Plan, CountEstimatesCost>> opCost = Stream.ofAll(planStepCosts).filter(pc -> pc.getPlan().getOps().contains(planOp)).toJavaOptional();
        if(!opCost.isPresent()){
            for (EntityJoinOp entityJoinOp : Stream.ofAll(planStepCosts).flatMap(plan -> plan.getPlan().getOps()).filter(op -> op instanceof EntityJoinOp).map(op -> (EntityJoinOp) op)) {
                PlanWithCost<Plan, CountEstimatesCost> joinCost = getPlanStepCost(entityJoinOp).get();
                opCost = ((JoinCost)joinCost.getCost()).getLeftBranchCost().getPlanStepCost(planOp);
                if(opCost.isPresent()){
                    break;
                }
                opCost = ((JoinCost)joinCost.getCost()).getRightBranchCost().getPlanStepCost(planOp);
                if(opCost.isPresent()){
                    break;
                }
            }
        }
        return opCost;
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        return new PlanDetailedCost(new DoubleCost(this.globalCost.cost), Stream.ofAll(this.planStepCosts).map(p -> {
            try {
                return new PlanWithCost<>(p.getPlan(), (CountEstimatesCost)p.getCost().clone());
            } catch (CloneNotSupportedException e) {
                return null;
            }
        }).toJavaList());
    }

    //endregion

    //region Fields
    private DoubleCost globalCost;
    private Iterable<PlanWithCost<Plan, CountEstimatesCost>> planStepCosts;
    //endregion


    @Override
    public String toString() {
        return " { " +
                "plan:" + new Plan(getPlanOps()).toString() + "," + "\n" +
                "estimation:" + (globalCost != null ? globalCost.toString() + "\n" : "")
                + " } ";
    }
}
