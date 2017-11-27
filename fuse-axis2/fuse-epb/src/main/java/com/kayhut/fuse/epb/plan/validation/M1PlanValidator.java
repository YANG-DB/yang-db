package com.kayhut.fuse.epb.plan.validation;

import com.codahale.metrics.Slf4jReporter;
import com.kayhut.fuse.dispatcher.utils.LoggerAnnotation;
import com.kayhut.fuse.dispatcher.utils.ValidationContext;
import com.kayhut.fuse.epb.plan.PlanValidator;
import com.kayhut.fuse.epb.plan.validation.opValidator.*;
import com.kayhut.fuse.model.asgQuery.AsgQuery;
import com.kayhut.fuse.model.execution.plan.composite.CompositePlanOp;
import com.kayhut.fuse.model.execution.plan.composite.Plan;
import javaslang.collection.Stream;

import java.util.Collections;

/**
 * Created by Roman on 04/05/2017.
 */
public class M1PlanValidator extends CompositePlanValidator<Plan,AsgQuery> {

    //region Constructors
    public M1PlanValidator() {
        super(Mode.all);

        this.validators = Collections.singletonList(new ChainedPlanValidator(buildNestedPlanOpValidator(10)));
    }
    //endregion

    //region CompositePlanValidator Implementation
    @Override
    @LoggerAnnotation(name = "isPlanValid", options = LoggerAnnotation.Options.full, logLevel = Slf4jReporter.LoggingLevel.DEBUG)
    public ValidationContext isPlanValid(Plan plan, AsgQuery query) {
        return super.isPlanValid(plan, query);
    }
    //endregion

    //region Private Methods
    private ChainedPlanValidator.PlanOpValidator buildNestedPlanOpValidator(int numNestingLevels) {
        if (numNestingLevels == 0) {
            return new ChainedPlanOpValidator(
                    new CompositePlanOpValidator(CompositePlanOpValidator.Mode.all,
                            new AdjacentPlanOpValidator(),
                            new NoRedundantRelationOpValidator(),
                            new RedundantGoToEntityOpValidator(),
                            new ReverseRelationOpValidator(),
                            new OptionalCompletePlanOpValidator()));
        }

        return new CompositePlanOpValidator(CompositePlanOpValidator.Mode.all,
                new AdjacentPlanOpValidator(),
                new NoRedundantRelationOpValidator(),
                new RedundantGoToEntityOpValidator(),
                new ReverseRelationOpValidator(),
                new OptionalCompletePlanOpValidator(),
                buildNestedPlanOpValidator(numNestingLevels - 1));
    }
    //endregion
}
