package com.kayhut.fuse.epb.plan.validation;

import com.kayhut.fuse.epb.plan.PlanValidator;
import javaslang.collection.Stream;

import java.util.ArrayList;
import java.util.function.IntFunction;

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
    public boolean isPlanValid(P plan, Q query) {
        for(PlanValidator<P, Q> validator : this.validators) {
            boolean isPlanValid = validator.isPlanValid(plan, query);

            if (isPlanValid && this.mode == Mode.one) {
                return true;
            }

            if (!isPlanValid && this.mode == Mode.all) {
                return false;
            }
        }

        return this.mode == Mode.all;
    }
    //endregion

    //region Fields
    private Mode mode;
    private Iterable<PlanValidator<P, Q>> validators;
    //endregion
}
