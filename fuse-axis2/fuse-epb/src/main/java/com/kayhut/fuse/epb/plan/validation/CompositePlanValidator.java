package com.kayhut.fuse.epb.plan.validation;

import com.kayhut.fuse.model.validation.QueryValidation;
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
    public QueryValidation isPlanValid(P plan, Q query) {
        for(PlanValidator<P, Q> validator : this.validators) {
            QueryValidation planValid = validator.isPlanValid(plan, query);

            if (planValid.valid() && this.mode == Mode.one) {
                return QueryValidation.OK;
            }

            if (!planValid.valid() && this.mode == Mode.all) {
                return planValid;
            }
        }

        if(this.mode == Mode.all) {
            return QueryValidation.OK;
        }

        return new QueryValidation(false,"Not all valid");
    }
    //endregion

    //region Fields
    protected Mode mode;
    protected Iterable<PlanValidator<P, Q>> validators;
    //endregion
}
