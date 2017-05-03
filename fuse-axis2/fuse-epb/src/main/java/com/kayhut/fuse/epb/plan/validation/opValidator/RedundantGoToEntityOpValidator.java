package com.kayhut.fuse.epb.plan.validation.opValidator;

import com.kayhut.fuse.epb.plan.validation.ChainedPlanValidator;
import com.kayhut.fuse.model.asgQuery.AsgQuery;
import com.kayhut.fuse.model.execution.plan.*;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by Roman on 30/04/2017.
 */
public class RedundantGoToEntityOpValidator implements ChainedPlanValidator.PlanOpValidator {
    //region Constructors
    public RedundantGoToEntityOpValidator() {
        this.entityEnums = new HashSet<>();
    }
    //endregion

    //region ChainedPlanValidator.PlanOpValidator Implementation
    @Override
    public void reset() {
        this.entityEnums.clear();
    }

    @Override
    public boolean isPlanOpValid(AsgQuery query, CompositePlanOpBase compositePlanOp, int opIndex) {
        PlanOpBase planOp = compositePlanOp.getOps().get(opIndex);
        if (planOp instanceof GoToEntityOp) {
            if (!this.entityEnums.contains(planOp.geteNum())) {
                return false;
            }
        }

        if (planOp instanceof EntityOp) {
            this.entityEnums.add(planOp.geteNum());
        }

        return true;
    }
    //endregion

    //region Fields
    private Set<Integer> entityEnums;
    //endregion
}
