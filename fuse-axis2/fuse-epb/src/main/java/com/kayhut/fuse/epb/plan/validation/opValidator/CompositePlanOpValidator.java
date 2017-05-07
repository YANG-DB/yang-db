package com.kayhut.fuse.epb.plan.validation.opValidator;

import com.kayhut.fuse.epb.plan.validation.ChainedPlanValidator;
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
    public CompositePlanOpValidator(Mode mode, ChainedPlanValidator.PlanOpValidator...planOpValidators) {
        this.mode = mode;
        this.planOpValidators = Stream.of(planOpValidators).toJavaList();
    }

    public CompositePlanOpValidator(Mode mode, Iterable<ChainedPlanValidator.PlanOpValidator> planOpValidators) {
        this.mode = mode;
        this.planOpValidators = Stream.ofAll(planOpValidators).toJavaList();
    }
    //endregion

    //region ChainedPlanValidator.PlanOpValidator Implementation
    @Override
    public void reset() {
        this.planOpValidators.forEach(ChainedPlanValidator.PlanOpValidator::reset);
    }

    @Override
    public boolean isPlanOpValid(AsgQuery query, CompositePlanOpBase compositePlanOp, int opIndex) {
        for(ChainedPlanValidator.PlanOpValidator planOpValidator : this.planOpValidators) {
            boolean isPlanOpValid = planOpValidator.isPlanOpValid(query, compositePlanOp, opIndex);

            if (isPlanOpValid && this.mode == Mode.one) {
                return true;
            }

            if (!isPlanOpValid && this.mode == Mode.all) {
                return false;
            }
        }

        return this.mode == Mode.all;
    }
    //endregion

    //region Fields
    private Iterable<ChainedPlanValidator.PlanOpValidator> planOpValidators;
    private Mode mode;
    //endregion
}
