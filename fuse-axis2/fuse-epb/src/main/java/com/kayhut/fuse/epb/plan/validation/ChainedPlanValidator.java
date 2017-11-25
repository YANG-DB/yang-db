package com.kayhut.fuse.epb.plan.validation;

import com.kayhut.fuse.dispatcher.utils.ValidationContext;
import com.kayhut.fuse.epb.plan.PlanValidator;
import com.kayhut.fuse.model.asgQuery.AsgQuery;
import com.kayhut.fuse.model.execution.plan.composite.CompositePlanOp;
import com.kayhut.fuse.model.execution.plan.composite.Plan;
import com.kayhut.fuse.model.execution.plan.PlanOp;
import com.kayhut.fuse.model.log.Trace;
import com.kayhut.fuse.model.log.TraceComposite;
import javaslang.Tuple2;

import java.util.List;
import java.util.logging.Level;

/**
 * Created by Roman on 24/04/2017.
 */
public class ChainedPlanValidator implements PlanValidator<Plan, AsgQuery> ,Trace<String>{
    private TraceComposite<String> trace = TraceComposite.build(this.getClass().getSimpleName());

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

    public interface PlanOpValidator extends Trace<String> {
        void reset();
        ValidationContext isPlanOpValid(AsgQuery query, CompositePlanOp compositePlanOp, int opIndex);
    }

    //region Constructors
    public ChainedPlanValidator(PlanOpValidator planOpValidator) {
        this.planOpValidator = planOpValidator;
        trace.with(planOpValidator);
    }
    //endregion

    //region PlanValidator Implementation
    @Override
    public ValidationContext isPlanValid(Plan plan, AsgQuery query) {
        this.planOpValidator.reset();

        int opIndex = 0;
        for (PlanOp planOp : plan.getOps()) {
            ValidationContext valid = planOpValidator.isPlanOpValid(query, plan, opIndex++);
            if(!valid.valid()) return valid;
        }

        return ValidationContext.OK;
    }
    //endregion

    //region Fields
    private PlanOpValidator planOpValidator;
    //endregion
}


