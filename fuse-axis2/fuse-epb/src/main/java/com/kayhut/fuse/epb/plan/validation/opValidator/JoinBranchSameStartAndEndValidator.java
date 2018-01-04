package com.kayhut.fuse.epb.plan.validation.opValidator;

import com.kayhut.fuse.dispatcher.utils.PlanUtil;
import com.kayhut.fuse.epb.plan.validation.ChainedPlanValidator;
import com.kayhut.fuse.model.asgQuery.AsgQuery;
import com.kayhut.fuse.model.execution.plan.PlanOp;
import com.kayhut.fuse.model.execution.plan.composite.CompositePlanOp;
import com.kayhut.fuse.model.execution.plan.composite.descriptors.IterablePlanOpDescriptor;
import com.kayhut.fuse.model.execution.plan.entity.EntityJoinOp;
import com.kayhut.fuse.model.execution.plan.entity.EntityOp;
import com.kayhut.fuse.model.validation.ValidationResult;

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

import static java.util.stream.Collectors.toList;

public class JoinBranchSameStartAndEndValidator implements ChainedPlanValidator.PlanOpValidator {

    @Override
    public void reset() {

    }

    @Override
    public ValidationResult isPlanOpValid(AsgQuery query, CompositePlanOp compositePlanOp, int opIndex) {
        if(compositePlanOp.getOps().get(opIndex) instanceof EntityJoinOp && !checkJoin((EntityJoinOp) compositePlanOp.getOps().get(opIndex))){
            return new ValidationResult(false, "A join branch cannot start and end in the same entity, " + IterablePlanOpDescriptor.getSimple().describe(compositePlanOp.getOps()));
        }
        return ValidationResult.OK;
    }

    private boolean checkJoin(EntityJoinOp joinOp){
        boolean valid = true;
        EntityOp firstLeftEntity = PlanUtil.first$(joinOp.getLeftBranch(), EntityOp.class);
        if(firstLeftEntity.getAsgEbase().geteBase().equals(joinOp.getAsgEbase().geteBase()) && !(firstLeftEntity instanceof EntityJoinOp))
            valid = false;

        EntityOp firstRightEntity = PlanUtil.first(joinOp.getRightBranch(), EntityOp.class).get();
        if(firstRightEntity.getAsgEbase().geteBase().equals(joinOp.getAsgEbase().geteBase()) && !(firstRightEntity instanceof EntityJoinOp))
            valid = false;


        return valid;
    }
}
