package com.kayhut.fuse.epb.plan.validation;

import com.kayhut.fuse.model.validation.ValidationResult;
import com.kayhut.fuse.epb.plan.validation.opValidator.*;
import com.kayhut.fuse.model.asgQuery.AsgQuery;
import com.kayhut.fuse.model.execution.plan.composite.Plan;

import java.util.Collections;

/**
 * Created by Roman on 04/05/2017.
 */
public class M1PlanValidator extends CompositePlanValidator<Plan, AsgQuery> {

    //region Constructors
    public M1PlanValidator() {
        super(Mode.all);

        this.validators = Collections.singletonList(new ChainedPlanValidator(buildNestedPlanOpValidator(10)));
    }
    //endregion

    //region CompositePlanValidator Implementation
    @Override
    public ValidationResult isPlanValid(Plan plan, AsgQuery query) {
        return super.isPlanValid(plan, query);
    }
    //endregion

    //region Private Methods
    private ChainedPlanValidator.PlanOpValidator buildNestedPlanOpValidator(int numNestingLevels) {
        if (numNestingLevels == 0) {
            return new CompositePlanOpValidator(CompositePlanOpValidator.Mode.all,
//                    new SingleEntityValidator(),
                    new AdjacentPlanOpValidator(),
                    new NoRedundantRelationOpValidator(),
                    new RedundantGoToEntityOpValidator(),
                    new ReverseRelationOpValidator(),
                    new OptionalCompletePlanOpValidator());
        }

        return new CompositePlanOpValidator(CompositePlanOpValidator.Mode.all,
//                new SingleEntityValidator(),
                new AdjacentPlanOpValidator(),
                new NoRedundantRelationOpValidator(),
                new RedundantGoToEntityOpValidator(),
                new ReverseRelationOpValidator(),
                new OptionalCompletePlanOpValidator(),
                new ChainedPlanOpValidator(buildNestedPlanOpValidator(numNestingLevels - 1)));
    }
    //endregion
}
