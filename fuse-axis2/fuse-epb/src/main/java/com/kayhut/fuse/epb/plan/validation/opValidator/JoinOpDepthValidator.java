package com.kayhut.fuse.epb.plan.validation.opValidator;

import com.kayhut.fuse.epb.plan.validation.ChainedPlanValidator;
import com.kayhut.fuse.model.asgQuery.AsgQuery;
import com.kayhut.fuse.model.execution.plan.composite.CompositePlanOp;
import com.kayhut.fuse.model.execution.plan.composite.descriptors.IterablePlanOpDescriptor;
import com.kayhut.fuse.model.execution.plan.entity.EntityJoinOp;
import com.kayhut.fuse.model.validation.ValidationResult;

/**
 * Validates join ops by checking nesting levels
 */
public class JoinOpDepthValidator implements ChainedPlanValidator.PlanOpValidator {
    public JoinOpDepthValidator(int maxDepth) {
        this.maxDepth = maxDepth;
    }

    public JoinOpDepthValidator() {
        this(3);
    }

    @Override
    public void reset() {

    }

    @Override
    public ValidationResult isPlanOpValid(AsgQuery query, CompositePlanOp compositePlanOp, int opIndex) {
        if(compositePlanOp.getOps().get(opIndex) instanceof EntityJoinOp){
            if(maxDepth <= 0){
                return new ValidationResult(false, "Too many nested joins , " + IterablePlanOpDescriptor.getSimple().describe(compositePlanOp.getOps()));
            }
        }
        return ValidationResult.OK;
    }
    private int maxDepth;
}
