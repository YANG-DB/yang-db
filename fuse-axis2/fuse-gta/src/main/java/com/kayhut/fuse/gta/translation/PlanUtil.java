package com.kayhut.fuse.gta.translation;

import com.kayhut.fuse.model.execution.plan.CompositePlanOpBase;
import com.kayhut.fuse.model.execution.plan.PlanOpBase;

import java.util.List;
import java.util.Optional;

/**
 * Created by benishue on 12-Mar-17.
 */
public class PlanUtil {
    //region Public Methods
    public static boolean isFirst(CompositePlanOpBase compositePlanOp, PlanOpBase planOpBase) {
        return compositePlanOp.getOps().size() > 0 && compositePlanOp.getOps().get(0) == planOpBase;
    }

    public static Optional<PlanOpBase> getNext(CompositePlanOpBase compositePlanOp, PlanOpBase planOpBase) {
        int indexOfCurrent = findIndexOfOp(compositePlanOp, planOpBase);
        return indexOfCurrent == compositePlanOp.getOps().size() - 1 ?
                Optional.empty() :
                Optional.of(compositePlanOp.getOps().get(++indexOfCurrent));
    }

    public static Optional<PlanOpBase> getPrev(CompositePlanOpBase compositePlanOp, PlanOpBase planOpBase) {
        int indexOfCurrent = findIndexOfOp(compositePlanOp, planOpBase);
        return indexOfCurrent == 0 ? Optional.empty() : Optional.of(compositePlanOp.getOps().get(--indexOfCurrent));
    }
    //endregion

    //region Private Methods
    private static int findIndexOfOp(CompositePlanOpBase compositePlanOp, PlanOpBase planOpBase) {
        for (int i = 0; i < compositePlanOp.getOps().size(); i++) {
            if (compositePlanOp.getOps().get(i) == planOpBase)
                return i;
        }
        return -1;
    }
    //endregion

}
