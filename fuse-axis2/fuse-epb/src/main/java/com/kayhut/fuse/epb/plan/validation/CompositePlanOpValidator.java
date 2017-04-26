package com.kayhut.fuse.epb.plan.validation;

import com.kayhut.fuse.model.asgQuery.AsgQuery;
import com.kayhut.fuse.model.execution.plan.CompositePlanOpBase;
import com.kayhut.fuse.model.execution.plan.PlanOpBase;
import javaslang.collection.Stream;

/**
 * Created by Roman on 24/04/2017.
 */
public class CompositePlanOpValidator implements ChainedPlanValidator.PlanOpValidator {
    public enum Mode {
        one,
        all
    }

    //region Constructors
    public CompositePlanOpValidator(Iterable<ChainedPlanValidator.PlanOpValidator> planOpValidators, Mode mode) {
        this.planOpValidators = planOpValidators;
        this.mode = mode;
    }
    //endregion

    //region ChainedPlanValidator.PlanOpValidator Implementation
    @Override
    public void reset() {
        this.planOpValidators.forEach(ChainedPlanValidator.PlanOpValidator::reset);
    }

    @Override
    public boolean isPlanOpValid(AsgQuery query, CompositePlanOpBase compositePlanOp, int opIndex) {
        boolean isPlanOpValid = false;
        for(ChainedPlanValidator.PlanOpValidator planOpValidator : this.planOpValidators) {
            isPlanOpValid |= planOpValidator.isPlanOpValid(query, compositePlanOp, opIndex);
            if (isPlanOpValid && this.mode == Mode.one) {
                return true;
            }
        }

        return isPlanOpValid;
    }
    //endregion

    //region Fields
    private Iterable<ChainedPlanValidator.PlanOpValidator> planOpValidators;
    private Mode mode;
    //endregion
}
