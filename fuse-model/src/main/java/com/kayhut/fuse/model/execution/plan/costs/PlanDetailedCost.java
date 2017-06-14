package com.kayhut.fuse.model.execution.plan.costs;

import com.kayhut.fuse.model.execution.plan.EntityOp;
import com.kayhut.fuse.model.execution.plan.PlanOpBase;
import com.kayhut.fuse.model.execution.plan.PlanOpWithCost;
import com.kayhut.fuse.model.query.entity.EEntityBase;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static com.kayhut.fuse.model.Utils.fullPattern;

/**
 * Created by Roman on 20/04/2017.
 */
public class PlanDetailedCost {
    public PlanDetailedCost() {
    }

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

    public List<PlanOpBase> getOps() {
        return StreamSupport.stream(getOpCosts().spliterator(), false).flatMap(p -> p.getOpBase().stream()).collect(Collectors.toList());
    }

    public Iterable<PlanOpWithCost<Cost>> getOpCosts() {
        return opCosts;
    }

    public Optional<PlanOpWithCost<Cost>> getPlanOpCost(PlanOpBase op) {
        return StreamSupport.stream(getOpCosts().spliterator(), false).filter(p -> p.getOpBase().contains(op)).findFirst();
    }

    public Optional<Cost> getOpCost(PlanOpBase planOpBase) {
        return StreamSupport.stream(opCosts.spliterator(), false).filter(p -> p.getOpBase().contains(planOpBase)).map(PlanOpWithCost::getCost).findFirst();
    }

    public Optional<PlanOpWithCost<Cost>> getPlanOpByEntity(EEntityBase entityBase) {
        return StreamSupport.stream(opCosts.spliterator(), false)
                .filter(p -> p.getOpBase().stream()
                        .anyMatch(op -> op instanceof EntityOp && ((EntityOp) op).getAsgEBase().geteBase().equals(entityBase)))
                .findFirst();
    }
    //endregion

    //region Fields
    private Cost globalCost;
    private Iterable<PlanOpWithCost<Cost>> opCosts;
    //endregion


    @Override
    public String toString() {
        return " { " +
                    "plan:" + fullPattern(getOps()) + "," + "\n" +
                    "cost:" + globalCost.toString() + "\n" +
                " } ";
    }
}
