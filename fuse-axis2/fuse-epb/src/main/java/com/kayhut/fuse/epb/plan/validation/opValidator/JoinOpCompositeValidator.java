package com.kayhut.fuse.epb.plan.validation.opValidator;

import com.kayhut.fuse.dispatcher.epb.PlanValidator;
import com.kayhut.fuse.epb.plan.validation.ChainedPlanValidator;
import com.kayhut.fuse.model.asgQuery.AsgQuery;
import com.kayhut.fuse.model.execution.plan.PlanOp;
import com.kayhut.fuse.model.execution.plan.composite.CompositePlanOp;
import com.kayhut.fuse.model.execution.plan.composite.Plan;
import com.kayhut.fuse.model.execution.plan.entity.EntityJoinOp;
import com.kayhut.fuse.model.validation.ValidationResult;

public class JoinOpCompositeValidator implements ChainedPlanValidator.PlanOpValidator{
    private PlanValidator<Plan, AsgQuery> leftValidator;
    private PlanValidator<Plan, AsgQuery> rightValidator;

    public JoinOpCompositeValidator(PlanValidator<Plan, AsgQuery> leftValidator, PlanValidator<Plan, AsgQuery> rightValidator) {
        this.leftValidator = leftValidator;
        this.rightValidator = rightValidator;
    }

    @Override
    public void reset() {

    }

    @Override
    public ValidationResult isPlanOpValid(AsgQuery query, CompositePlanOp compositePlanOp, int opIndex) {
        PlanOp planOp = compositePlanOp.getOps().get(opIndex);
        if(planOp instanceof EntityJoinOp){
            EntityJoinOp joinOp = (EntityJoinOp) planOp;
            ValidationResult leftValidationContext = this.leftValidator.isPlanValid(joinOp.getLeftBranch(), query);
            if(leftValidationContext.valid()){
                ValidationResult rightValidationContext = this.rightValidator.isPlanValid(joinOp.getRightBranch(), query);
                if(!rightValidationContext.valid()){
                    return rightValidationContext;
                }
            }else
                return leftValidationContext;
        }
        return ValidationResult.OK;
    }
}
