package com.kayhut.fuse.model.execution.plan.costs;

import com.kayhut.fuse.model.execution.plan.PlanOpBase;
import com.kayhut.fuse.model.execution.plan.PlanOpWithCost;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.StreamSupport;

/**
 * Created by Roman on 20/04/2017.
 */
public class PlanDetailedCost {
    public PlanDetailedCost() {}

    public PlanDetailedCost(Cost globalCost, Iterable<PlanOpWithCost<Cost>> opCosts) {
        this.globalCost = globalCost;
        this.opCosts = opCosts;
    }

    public PlanDetailedCost(PlanDetailedCost formerCost) {
        //todo implement clone
        this(formerCost.globalCost, formerCost.opCosts);
    }
    //region properties

    public Cost getGlobalCost() {
        return globalCost;
    }

    public Iterable<PlanOpWithCost<Cost>> getOpCosts() {
        return opCosts;
    }

    public Optional<PlanOpWithCost<Cost>> getPlanOpCost(PlanOpBase op) {
        return StreamSupport.stream(getOpCosts().spliterator(), false).filter(p->p.getOpBase().equals(op)).findFirst();
    }

    public Optional<Cost> getOpLatestCost(PlanOpBase planOpBase){
        return StreamSupport.stream(opCosts.spliterator(),false).filter(p->p.getOpBase().contains(planOpBase)).map(PlanOpWithCost::getCost).findFirst();
    }
    //endregion

    //region Fields
    private Cost globalCost;
    private Iterable<PlanOpWithCost<Cost>> opCosts;
    //endregion


}
