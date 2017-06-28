package com.kayhut.fuse.model.execution.plan.costs;

import com.kayhut.fuse.model.execution.plan.*;
import javaslang.collection.Stream;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static com.kayhut.fuse.model.Utils.fullPattern;

/**
 * Created by Roman on 20/04/2017.
 */
public class PlanDetailedCost implements Cost {
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

    public List<PlanOpBase> getPlanOps() {
        if (planStepCosts != null) {
            return Stream.ofAll(planStepCosts).flatMap(pc -> Stream.ofAll(pc.getPlan().getOps())).toJavaList();
        }

        return Collections.emptyList();
    }

    public Iterable<PlanWithCost<Plan, CountEstimatesCost>> getPlanStepCosts() {
        return planStepCosts;
    }

    public Optional<PlanWithCost<Plan, CountEstimatesCost>> getPlanStepCost(PlanOpBase planOp) {
        return Stream.ofAll(planStepCosts).filter(pc -> pc.getPlan().getOps().contains(planOp)).toJavaOptional();
    }
    //endregion

    //region Fields
    private DoubleCost globalCost;
    private Iterable<PlanWithCost<Plan, CountEstimatesCost>> planStepCosts;
    //endregion


    @Override
    public String toString() {
        return " { " +
                "plan:" + fullPattern(getPlanOps()) + "," + "\n" +
                "estimation:" + (globalCost != null ? globalCost.toString() + "\n" : "")
                + " } ";
    }
}
