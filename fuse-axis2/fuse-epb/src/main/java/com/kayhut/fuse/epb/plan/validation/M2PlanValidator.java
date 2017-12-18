package com.kayhut.fuse.epb.plan.validation;

import com.kayhut.fuse.epb.plan.validation.opValidator.*;
import com.kayhut.fuse.model.asgQuery.AsgQuery;
import com.kayhut.fuse.model.execution.plan.composite.Plan;
import com.kayhut.fuse.model.validation.QueryValidation;

import java.util.Arrays;

/**
 * Created by Roman on 04/05/2017.
 */
public class M2PlanValidator extends CompositePlanValidator<Plan,AsgQuery> {

    //region Constructors
    public M2PlanValidator() {
        super(Mode.all);

        //this.validators = Collections.singletonList(new ChainedPlanValidator(buildNestedPlanOpValidator(10)));
        this.validators = Arrays.asList(new ChainedPlanValidator(buildNestedPlanOpValidator(10)));
    }
    //endregion

    //region CompositePlanValidator Implementation
    @Override
    public QueryValidation isPlanValid(Plan plan, AsgQuery query) {
        return super.isPlanValid(plan, query);
    }
    //endregion

    //region Private Methods
    private ChainedPlanValidator.PlanOpValidator buildNestedPlanOpValidator(int numNestingLevels) {
        if (numNestingLevels == 0) {
                    return new CompositePlanOpValidator(CompositePlanOpValidator.Mode.all,
                            new AdjacentPlanOpValidator(),
                            new NoRedundantRelationOpValidator(),
                            new RedundantGoToEntityOpValidator(),
                            new ReverseRelationOpValidator(),
                            new OptionalCompletePlanOpValidator(),
                            new JoinCompletePlanOpValidator(),
                            new JoinIntersectionPlanOpValidator(),
                            new StraightPathJoinOpValidator(),
                            new JoinOpCompositeValidator(
                                new ChainedPlanValidator(new CompositePlanOpValidator(CompositePlanOpValidator.Mode.all, new AdjacentPlanOpValidator(),
                                    new NoRedundantRelationOpValidator(),
                                    new RedundantGoToEntityOpValidator(),
                                    new ReverseRelationOpValidator(),
                                    new OptionalCompletePlanOpValidator(),
                                    new JoinCompletePlanOpValidator(true),
                                    new JoinIntersectionPlanOpValidator(),
                                    new StraightPathJoinOpValidator())),
                                new ChainedPlanValidator(new CompositePlanOpValidator(CompositePlanOpValidator.Mode.all, new AdjacentPlanOpValidator(),
                                        new NoRedundantRelationOpValidator(),
                                        new RedundantGoToEntityOpValidator(),
                                        new ReverseRelationOpValidator(),
                                        new OptionalCompletePlanOpValidator(),
                                        new JoinCompletePlanOpValidator(),
                                        new JoinIntersectionPlanOpValidator(),
                                        new StraightPathJoinOpValidator()))));
        }
        ChainedPlanValidator.PlanOpValidator planOpValidator = buildNestedPlanOpValidator(numNestingLevels - 1);
        return new CompositePlanOpValidator(CompositePlanOpValidator.Mode.all,
                new AdjacentPlanOpValidator(),
                new NoRedundantRelationOpValidator(),
                new RedundantGoToEntityOpValidator(),
                new ReverseRelationOpValidator(),
                new OptionalCompletePlanOpValidator(),
                new JoinCompletePlanOpValidator(),
                new JoinIntersectionPlanOpValidator(),
                new StraightPathJoinOpValidator(),
                new JoinOpCompositeValidator(new ChainedPlanValidator(new CompositePlanOpValidator(CompositePlanOpValidator.Mode.all, new AdjacentPlanOpValidator(),
                        new NoRedundantRelationOpValidator(),
                        new RedundantGoToEntityOpValidator(),
                        new ReverseRelationOpValidator(),
                        new OptionalCompletePlanOpValidator(),
                        new JoinCompletePlanOpValidator(true),
                        new JoinIntersectionPlanOpValidator(),
                        new StraightPathJoinOpValidator())),
                        new ChainedPlanValidator(planOpValidator)),
                new ChainedPlanOpValidator(planOpValidator)
                );
    }
    //endregion
}
