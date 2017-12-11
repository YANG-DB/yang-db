package com.kayhut.fuse.epb.plan.validation.opValidator;

import com.kayhut.fuse.dispatcher.utils.ValidationContext;
import com.kayhut.fuse.epb.plan.validation.ChainedPlanValidator;
import com.kayhut.fuse.model.asgQuery.AsgQuery;
import com.kayhut.fuse.model.execution.plan.composite.CompositePlanOp;
import com.kayhut.fuse.model.log.Trace;
import com.kayhut.fuse.model.log.TraceComposite;
import javaslang.Tuple2;
import javaslang.collection.Stream;

import java.util.List;
import java.util.logging.Level;

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

    //region Public Method
    public CompositePlanOpValidator with(ChainedPlanValidator.PlanOpValidator planOpValidator) {
        this.planOpValidators.add(planOpValidator);
        return this;
    }
    //endregion

    //region ChainedPlanValidator.PlanOpValidator Implementation
    @Override
    public void reset() {
        this.planOpValidators.forEach(ChainedPlanValidator.PlanOpValidator::reset);
    }

    @Override
    public ValidationContext isPlanOpValid(AsgQuery query, CompositePlanOp compositePlanOp, int opIndex) {
        for(ChainedPlanValidator.PlanOpValidator planOpValidator : this.planOpValidators) {
            ValidationContext planOpValid = planOpValidator.isPlanOpValid(query, compositePlanOp, opIndex);

            if (planOpValid.valid() && this.mode == Mode.one) {
                return ValidationContext.OK;
            }

            if (!planOpValid.valid() && this.mode == Mode.all) {
                return planOpValid;
            }
        }

        if(this.mode == Mode.all) {
            return ValidationContext.OK;
        }

        return new ValidationContext(false, "Not all valid");
    }
    //endregion

    //region Fields
    private List<ChainedPlanValidator.PlanOpValidator> planOpValidators;
    private Mode mode;
    //endregion
}
