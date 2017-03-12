package com.kayhut.fuse.gta.translation;

import com.kayhut.fuse.model.execution.plan.PlanOpBase;

import java.util.List;
import java.util.ListIterator;
import java.util.Optional;

/**
 * Created by benishue on 12-Mar-17.
 */
public class PlanUtil {

    //region Plan Util Class

        public boolean isFirst(List<PlanOpBase> ops, PlanOpBase planOpBase) {
            if (ops.size()>0)
                return ops.get(0) == planOpBase;
            return false;
        }

        public Optional<PlanOpBase> getNext(List<PlanOpBase> ops, PlanOpBase planOpBase)
        {
            int indexOfCurrent = ops.indexOf(planOpBase);
            return indexOfCurrent == ops.size() - 1 ? Optional.empty() : Optional.of(ops.get(++indexOfCurrent));
        }

        public Optional<PlanOpBase> getPrev(List<PlanOpBase> ops, PlanOpBase planOpBase)
        {
            int indexOfCurrent = ops.indexOf(planOpBase);
            return indexOfCurrent == 0 ? Optional.empty() : Optional.of(ops.get(--indexOfCurrent));
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

    //endregion
}
