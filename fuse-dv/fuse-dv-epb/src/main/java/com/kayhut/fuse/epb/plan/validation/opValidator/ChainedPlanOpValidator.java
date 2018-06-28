package com.kayhut.fuse.epb.plan.validation.opValidator;

import com.kayhut.fuse.model.validation.ValidationResult;
import com.kayhut.fuse.epb.plan.validation.ChainedPlanValidator;
import com.kayhut.fuse.model.asgQuery.AsgQuery;
import com.kayhut.fuse.model.execution.plan.PlanOp;
import com.kayhut.fuse.model.execution.plan.composite.CompositePlanOp;

/**
 * Created by Roman on 11/26/2017.
 */
public class ChainedPlanOpValidator implements ChainedPlanValidator.PlanOpValidator {
    //region Constructors
    public ChainedPlanOpValidator(ChainedPlanValidator.PlanOpValidator planOpValidator) {
        this.planOpValidator = planOpValidator;
    }
    //endregion

    //region PlanOpValidator Implementation
    @Override
    public void reset() {

    }

    @Override
    public ValidationResult isPlanOpValid(AsgQuery query, CompositePlanOp compositePlanOp, int opIndex) {
        PlanOp currentPlanOp = compositePlanOp.getOps().get(opIndex);
        if (!CompositePlanOp.class.isAssignableFrom(currentPlanOp.getClass())) {
            return ValidationResult.OK;
        }

        CompositePlanOp currentCompositePlanOp = (CompositePlanOp)currentPlanOp;
        this.planOpValidator.reset();
        for (int innerOpIndex = 0 ; innerOpIndex < currentCompositePlanOp.getOps().size() ; innerOpIndex++) {
            ValidationResult valid = planOpValidator.isPlanOpValid(query, currentCompositePlanOp, innerOpIndex);
            if(!valid.valid()) {
                return valid;
            }
        }

        return ValidationResult.OK;
    }
    //endregion

    //region Fields
    private ChainedPlanValidator.PlanOpValidator planOpValidator;
    //endregion
}
