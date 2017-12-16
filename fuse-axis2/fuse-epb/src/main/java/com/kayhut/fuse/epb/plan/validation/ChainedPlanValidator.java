package com.kayhut.fuse.epb.plan.validation;

import com.kayhut.fuse.model.validation.QueryValidation;
import com.kayhut.fuse.dispatcher.epb.PlanValidator;
import com.kayhut.fuse.model.asgQuery.AsgQuery;
import com.kayhut.fuse.model.execution.plan.composite.CompositePlanOp;
import com.kayhut.fuse.model.execution.plan.composite.Plan;

/**
 * Created by Roman on 24/04/2017.
 */
public class ChainedPlanValidator implements PlanValidator<Plan, AsgQuery> {

    public interface PlanOpValidator {
        void reset();
        QueryValidation isPlanOpValid(AsgQuery query, CompositePlanOp compositePlanOp, int opIndex);
    }

    //region Constructors
    public ChainedPlanValidator(PlanOpValidator planOpValidator) {
        this.planOpValidator = planOpValidator;
    }
    //endregion

    //region PlanValidator Implementation
    @Override
    public QueryValidation isPlanValid(Plan plan, AsgQuery query) {
        this.planOpValidator.reset();

        for (int opIndex = 0 ; opIndex < plan.getOps().size() ; opIndex++) {
            QueryValidation valid = planOpValidator.isPlanOpValid(query, plan, opIndex);
            if(!valid.valid()) {
                return valid;
            }
        }

        return QueryValidation.OK;
    }
    //endregion

    //region Fields
    private PlanOpValidator planOpValidator;
    //endregion
}


