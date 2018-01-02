package com.kayhut.fuse.epb.plan.validation;

import com.kayhut.fuse.epb.plan.validation.opValidator.*;
import com.kayhut.fuse.model.asgQuery.AsgQuery;
import com.kayhut.fuse.model.execution.plan.composite.Plan;
import com.kayhut.fuse.model.validation.ValidationResult;

import java.util.Arrays;

/**
 *
 */
// TODO: Fix this to support join op depth validation
public class M2PlanValidator extends CompositePlanValidator<Plan,AsgQuery> {

    //region Constructors
    public M2PlanValidator() {
        super(Mode.all);

        //this.validators = Collections.singletonList(new ChainedPlanValidator(buildNestedPlanOpValidator(10)));
        this.validators = Arrays.asList(new ChainedPlanValidator(buildNestedPlanOpValidator(10,3 )));
    }
    //endregion

    //region CompositePlanValidator Implementation
    @Override
    public ValidationResult isPlanValid(Plan plan, AsgQuery query) {
        return super.isPlanValid(plan, query);
    }
    //endregion

    //region Private Methods
    private ChainedPlanValidator.PlanOpValidator buildNestedPlanOpValidator(int numNestingLevels, int joinDepth) {
        if (numNestingLevels == 0) {
                    return new CompositePlanOpValidator(CompositePlanOpValidator.Mode.all,
                            new AdjacentPlanOpValidator(),
                            new NoRedundantRelationOpValidator(),
                            new RedundantGoToEntityOpValidator(),
                            new ReverseRelationOpValidator(),
                            new OptionalCompletePlanOpValidator(),
                            new JoinCompletePlanOpValidator(),
                            new JoinIntersectionPlanOpValidator(),
                            new JoinOpDepthValidator(joinDepth),
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
                                        new JoinOpDepthValidator(joinDepth-1),
                                        new JoinIntersectionPlanOpValidator(),
                                        new StraightPathJoinOpValidator()))));
        }
        ChainedPlanValidator.PlanOpValidator planOpValidator = buildNestedPlanOpValidator(numNestingLevels - 1, joinDepth-1);
        return new CompositePlanOpValidator(CompositePlanOpValidator.Mode.all,
                new AdjacentPlanOpValidator(),
                new NoRedundantRelationOpValidator(),
                new RedundantGoToEntityOpValidator(),
                new ReverseRelationOpValidator(),
                new OptionalCompletePlanOpValidator(),
                new JoinCompletePlanOpValidator(),
                new JoinIntersectionPlanOpValidator(),
                new JoinOpDepthValidator(joinDepth),
                new StraightPathJoinOpValidator(),
                new JoinOpCompositeValidator(new ChainedPlanValidator(new CompositePlanOpValidator(CompositePlanOpValidator.Mode.all, new AdjacentPlanOpValidator(),
                        new NoRedundantRelationOpValidator(),
                        new RedundantGoToEntityOpValidator(),
                        new ReverseRelationOpValidator(),
                        new OptionalCompletePlanOpValidator(),
                        new JoinCompletePlanOpValidator(true),
                        new JoinIntersectionPlanOpValidator(),
                        new JoinOpDepthValidator(joinDepth-1),
                        new StraightPathJoinOpValidator())),
                        new ChainedPlanValidator(planOpValidator)),
                new ChainedPlanOpValidator(planOpValidator)
                );
    }
    //endregion
}
