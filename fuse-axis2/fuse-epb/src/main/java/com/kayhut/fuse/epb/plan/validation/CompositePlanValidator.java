package com.kayhut.fuse.epb.plan.validation;

import com.kayhut.fuse.dispatcher.utils.ValidationContext;
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
    public ValidationContext isPlanValid(P plan, Q query) {
        for(PlanValidator<P, Q> validator : this.validators) {
            ValidationContext planValid = validator.isPlanValid(plan, query);

            if (planValid.valid() && this.mode == Mode.one) {
                return ValidationContext.OK;
            }

            if (!planValid.valid() && this.mode == Mode.all) {
                return planValid;
            }
        }

        if(this.mode == Mode.all) {
            return ValidationContext.OK;
        }

        return new ValidationContext(false,"Not all valid");
    }
    //endregion

    //region Fields
    protected Mode mode;
    protected Iterable<PlanValidator<P, Q>> validators;
    //endregion
}
