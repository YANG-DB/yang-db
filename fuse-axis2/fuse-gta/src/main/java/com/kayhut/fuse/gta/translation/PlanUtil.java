package com.kayhut.fuse.gta.translation;

import com.kayhut.fuse.model.execution.plan.PlanOpBase;
import com.kayhut.fuse.model.execution.plan.PlanOpWithCost;

import java.util.List;
import java.util.Optional;

/**
 * Created by benishue on 12-Mar-17.
 */
public class PlanUtil {

    //region Plan Util Class

        public boolean isFirst(List<? extends PlanOpWithCost> ops, PlanOpBase planOpBase) {
            return ops.size() > 0 && ops.get(0).getOpBase() == planOpBase;
        }

        public Optional<PlanOpBase> getNext(List<? extends PlanOpWithCost> ops, PlanOpBase planOpBase)
        {
            int indexOfCurrent = findIndexOfOp(ops, planOpBase);
            return indexOfCurrent == ops.size() - 1 ? Optional.empty() : Optional.of(ops.get(++indexOfCurrent).getOpBase());
        }

        public Optional<PlanOpBase> getPrev(List<? extends PlanOpWithCost> ops, PlanOpBase planOpBase)
        {
            int indexOfCurrent = findIndexOfOp(ops, planOpBase);
            return indexOfCurrent == 0 ? Optional.empty() : Optional.of(ops.get(--indexOfCurrent).getOpBase());
        }

        private int findIndexOfOp(List<? extends PlanOpWithCost> ops, PlanOpBase planOpBase){
            for (int i = 0;i < ops.size(); i++){
                if(ops.get(i).getOpBase() == planOpBase)
                    return i;
            }
            return -1;
        }


    //endregion
}
