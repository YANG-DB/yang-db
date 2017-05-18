package com.kayhut.fuse.epb.plan.validation;

import com.kayhut.fuse.epb.plan.PlanValidator;
import com.kayhut.fuse.epb.plan.validation.opValidator.RedundantGoToEntityOpValidator;
import com.kayhut.fuse.model.log.Trace;
import com.kayhut.fuse.model.log.TraceComposite;
import javaslang.Tuple2;
import javaslang.collection.Stream;

import java.util.List;
import java.util.logging.Level;

/**
 * Created by Roman on 30/04/2017.
 */
public class CompositePlanValidator<P, Q> implements PlanValidator<P, Q>, TraceComposite<String> {
    private TraceComposite<String> trace = TraceComposite.build(this.getClass().getSimpleName());

    @Override
    public void log(String event, Level level) {
        trace.log(event,level);
    }

    @Override
    public List<Tuple2<String,String>> getLogs(Level level) {
        return trace.getLogs(level);
    }

    @Override
    public void with(Trace<String> trace) {
        this.trace.with(trace);
    }

    @Override
    public String who() {
        return trace.who();
    }

    public enum Mode {
        one,
        all
    }

    //region Constructors
    public CompositePlanValidator(Mode mode, PlanValidator<P, Q>...validators) {
        this.mode = mode;
        this.validators = Stream.of(validators).toJavaList();
        this.validators.forEach(this::with);
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
