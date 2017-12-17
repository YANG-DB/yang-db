package com.kayhut.fuse.epb.plan.validation.opValidator;

import com.kayhut.fuse.model.validation.QueryValidation;
import com.kayhut.fuse.epb.plan.validation.ChainedPlanValidator;
import com.kayhut.fuse.model.asgQuery.AsgQuery;
import com.kayhut.fuse.model.execution.plan.*;
import com.kayhut.fuse.model.execution.plan.composite.CompositePlanOp;
import com.kayhut.fuse.model.execution.plan.entity.EntityJoinOp;
import com.kayhut.fuse.model.execution.plan.entity.EntityOp;
import com.kayhut.fuse.model.execution.plan.entity.GoToEntityOp;
import com.kayhut.fuse.model.results.Entity;

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
    public QueryValidation isPlanOpValid(AsgQuery query, CompositePlanOp compositePlanOp, int opIndex) {
        PlanOp planOp = compositePlanOp.getOps().get(opIndex);
        if (planOp instanceof GoToEntityOp) {
            if (!this.entityEnums.contains(((AsgEBaseContainer)planOp).getAsgEbase().geteNum())) {
                return new QueryValidation(
                        false,
                        "GoTo:Validation failed on:" +   compositePlanOp.toString() + "<" + opIndex + ">");
            }
        }

        if (planOp instanceof EntityOp) {
            this.entityEnums.add(((AsgEBaseContainer)planOp).getAsgEbase().geteNum());
        }


        if(planOp instanceof EntityJoinOp){
            recursiveEntityNums((EntityJoinOp) planOp);
        }

        return QueryValidation.OK;

    }
    //endregion

    private void recursiveEntityNums(EntityJoinOp joinOp){
        joinOp.getLeftBranch().getOps().forEach(op -> {
            if(op instanceof EntityOp){
                this.entityEnums.add(((AsgEBaseContainer)op).getAsgEbase().geteNum());
            }
            if(op instanceof EntityJoinOp){
                recursiveEntityNums((EntityJoinOp) op);
            }
        });

        joinOp.getRightBranch().getOps().forEach(op -> {
            if(op instanceof EntityOp){
                this.entityEnums.add(((AsgEBaseContainer)op).getAsgEbase().geteNum());
            }
            if(op instanceof EntityJoinOp){
                recursiveEntityNums((EntityJoinOp) op);
            }
        });

    }

    //region Fields
    private Set<Integer> entityEnums;
    //endregion
}
