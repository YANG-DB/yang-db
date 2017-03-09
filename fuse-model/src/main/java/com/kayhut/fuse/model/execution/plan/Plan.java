package com.kayhut.fuse.model.execution.plan;

import java.util.*;

import com.google.common.collect.PeekingIterator;
import static com.google.common.collect.Iterators.peekingIterator;

/**
 * Created by User on 22/02/2017.
 */
public class Plan {
    //region Constructors
    public Plan() {

    }

    public Plan(List<PlanOpBase> ops) {
        this.ops = ops;
    }
    //endregion

    //region Properties
    public List<PlanOpBase> getOps() {
        return this.ops;
    }

    public void setOps(List<PlanOpBase> ops) {
        this.ops = ops;
    }
    //endregion

    public boolean isFirst(PlanOpBase planOpBase) {
        return this.ops.get(0) == planOpBase;
    }


    //Impl of previous for iterator

//    public PlanOpBase getPrev(PlanOpBase planOpBase) {
//        PlanOpBase previousPlanOpBase = null;
//        for (Iterator<PlanOpBase> i = this.ops; this.ops.hasNext();) {
//            PlanOpBase currentPlanOpBase = i.next();
//
//            if (previousPlanOpBase != null && currentPlanOpBase.equals(planOpBase))
//                break;
//            previousPlanOpBase = currentPlanOpBase;
//        }
//        return  previousPlanOpBase;
//    }


    // Impl for List
    public Optional<PlanOpBase> getPrev(PlanOpBase planOpBase) {
        Optional<PlanOpBase> previousPlanOpBase = Optional.empty();
        for (PlanOpBase currPlanOpBase : this.ops) {
            if (previousPlanOpBase.isPresent() && currPlanOpBase.equals(planOpBase)) {
                break;
            }
            previousPlanOpBase = Optional.of(currPlanOpBase);
        }
        return  previousPlanOpBase;
    }


    //region Fields
    private List<PlanOpBase> ops;
    //endregion
}
