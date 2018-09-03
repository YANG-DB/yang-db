package com.kayhut.fuse.epb.plan.validation;

import com.kayhut.fuse.model.validation.ValidationResult;
import com.kayhut.fuse.dispatcher.epb.PlanValidator;
import javaslang.collection.Stream;

/**
 * Created by Roman on 30/04/2017.
 */
public class CompositePlanValidator<P, Q> implements PlanValidator<P, Q> {
    public enum Mode {
        one,
        all
    }

    //region Constructors
    public CompositePlanValidator(Mode mode, PlanValidator<P, Q>...validators) {
        this.mode = mode;
        this.validators = Stream.of(validators).toJavaList();
    }

    public CompositePlanValidator(Mode mode, Iterable<PlanValidator<P, Q>> validators) {
        this.mode = mode;
        this.validators = Stream.ofAll(validators).toJavaList();
    }
    //endregion

    //region PlanValidator Implementation
    @Override
    public ValidationResult isPlanValid(P plan, Q query) {
        for(PlanValidator<P, Q> validator : this.validators) {
            ValidationResult planValid = validator.isPlanValid(plan, query);

            if (planValid.valid() && this.mode == Mode.one) {
                return ValidationResult.OK;
            }

            if (!planValid.valid() && this.mode == Mode.all) {
                return planValid;
            }
        }

        if(this.mode == Mode.all) {
            return ValidationResult.OK;
        }

        return new ValidationResult(false,this.getClass().getSimpleName(),"Not all valid");
    }
    //endregion

    //region Fields
    protected Mode mode;
    protected Iterable<PlanValidator<P, Q>> validators;
    //endregion
}
