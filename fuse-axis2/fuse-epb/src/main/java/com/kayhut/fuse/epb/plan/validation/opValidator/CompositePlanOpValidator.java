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
public class CompositePlanOpValidator implements ChainedPlanValidator.PlanOpValidator , TraceComposite<String>{
    private TraceComposite<String> trace = TraceComposite.build(this.getClass().getSimpleName());

    public enum Mode {
        one,
        all
    }

    @Override
    public void log(String event, Level level) {
        trace.log(event,level);
    }

    @Override
    public List<Tuple2<String, String>> getLogs(Level level) {
        return trace.getLogs(level);
    }

    @Override
    public String who() {
        return trace.who();
    }

    @Override
    public void with(Trace<String> trace) {
        this.trace.with(trace);
    }

    //region Constructors
    public CompositePlanOpValidator(Mode mode, ChainedPlanValidator.PlanOpValidator...planOpValidators) {
        this.mode = mode;
        this.planOpValidators = Stream.of(planOpValidators).toJavaList();
        this.planOpValidators.forEach(p->trace.with(p));
    }

    public CompositePlanOpValidator(Mode mode, Iterable<ChainedPlanValidator.PlanOpValidator> planOpValidators) {
        this.mode = mode;
        this.planOpValidators = Stream.ofAll(planOpValidators).toJavaList();
        this.planOpValidators.forEach(p->trace.with(p));
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

        return new ValidationContext(false,"Not all valid");
    }
    //endregion

    //region Fields
    private Iterable<ChainedPlanValidator.PlanOpValidator> planOpValidators;
    private Mode mode;
    //endregion
}
