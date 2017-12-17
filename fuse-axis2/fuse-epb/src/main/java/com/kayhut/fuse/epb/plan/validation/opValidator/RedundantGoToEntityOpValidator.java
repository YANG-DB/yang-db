package com.kayhut.fuse.epb.plan.validation.opValidator;

import com.kayhut.fuse.model.validation.ValidationResult;
import com.kayhut.fuse.epb.plan.validation.ChainedPlanValidator;
import com.kayhut.fuse.model.asgQuery.AsgQuery;
import com.kayhut.fuse.model.execution.plan.*;
import com.kayhut.fuse.model.execution.plan.composite.CompositePlanOp;
import com.kayhut.fuse.model.execution.plan.entity.EntityOp;
import com.kayhut.fuse.model.execution.plan.entity.GoToEntityOp;

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
    public ValidationResult isPlanOpValid(AsgQuery query, CompositePlanOp compositePlanOp, int opIndex) {
        PlanOp planOp = compositePlanOp.getOps().get(opIndex);
        if (planOp instanceof GoToEntityOp) {
            if (!this.entityEnums.contains(((AsgEBaseContainer)planOp).getAsgEbase().geteNum())) {
                return new ValidationResult(
                        false,
                        "GoTo:Validation failed on:" +   compositePlanOp.toString() + "<" + opIndex + ">");
            }
        }

        if (planOp instanceof EntityOp) {
            this.entityEnums.add(((AsgEBaseContainer)planOp).getAsgEbase().geteNum());
        }

        return ValidationResult.OK;
    }
    //endregion

    //region Fields
    private Set<Integer> entityEnums;
    //endregion
}
