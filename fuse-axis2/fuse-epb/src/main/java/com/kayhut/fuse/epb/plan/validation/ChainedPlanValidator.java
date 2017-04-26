package com.kayhut.fuse.epb.plan.validation;

import com.kayhut.fuse.epb.plan.PlanValidator;
import com.kayhut.fuse.model.asgQuery.AsgQuery;
import com.kayhut.fuse.model.execution.plan.CompositePlanOpBase;
import com.kayhut.fuse.model.execution.plan.Plan;
import com.kayhut.fuse.model.execution.plan.PlanOpBase;

import java.util.Optional;

/**
 * Created by Roman on 24/04/2017.
 */
public class ChainedPlanValidator implements PlanValidator<Plan, AsgQuery> {
    public interface PlanOpValidator {
        void reset();
        boolean isPlanOpValid(AsgQuery query, CompositePlanOpBase compositePlanOp, int opIndex);
    }

    //region Constructors
    public ChainedPlanValidator(PlanOpValidator planOpValidator) {
        this.planOpValidator = planOpValidator;
    }
    //endregion

    //region PlanValidator Implementation
    @Override
    public boolean isPlanValid(Plan plan, AsgQuery query) {
        this.planOpValidator.reset();

        int opIndex = 0;
        for (PlanOpBase planOp : plan.getOps()) {
            if (!planOpValidator.isPlanOpValid(query, plan, opIndex++)) {
                return false;
            }
        }

        return true;
    }
    //endregion

    //region Fields
    private PlanOpValidator planOpValidator;
    //endregion
}


