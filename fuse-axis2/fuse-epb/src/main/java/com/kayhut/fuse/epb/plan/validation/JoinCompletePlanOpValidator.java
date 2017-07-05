package com.kayhut.fuse.epb.plan.validation;

import com.kayhut.fuse.model.execution.plan.PlanUtil;
import com.kayhut.fuse.dispatcher.utils.ValidationContext;
import com.kayhut.fuse.epb.plan.PlanValidator;
import com.kayhut.fuse.model.asgQuery.AsgQuery;
import com.kayhut.fuse.model.execution.plan.*;
import com.kayhut.fuse.model.log.Trace;
import javaslang.Tuple2;

import java.util.List;
import java.util.Optional;
import java.util.logging.Level;

import static com.kayhut.fuse.model.execution.plan.Plan.toPattern;

/**
 * Created by benishue on 7/4/2017.
 */
public class JoinCompletePlanOpValidator implements PlanValidator<Plan, AsgQuery> {
    private Trace<String> trace = Trace.build(JoinCompletePlanOpValidator.class.getSimpleName());

    //region PlanValidator Implementation
    @Override
    public void log(String event, Level level) {
        trace.log(event, level);
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
    public ValidationContext isPlanValid(Plan plan, AsgQuery query) {
        Optional<JoinOp> joinOp = PlanUtil.first(plan, JoinOp.class);
        /*
        We need to check validity only if the # of Ops is at least 2, if so, we need to check
        that this JoinOp is a 'Complete' one (See function below).
        The 'Extenders' (according to Mr. R) will always make sure that the JoinOp will be the first element.
        */
        if (plan.getOps().size() > 1 && joinOp.isPresent() && PlanUtil.isFirst(plan, joinOp.get())
                && !isJoinOpComplete(joinOp.get())) {
            return new ValidationContext(false, "JoinOp Validation failed: " + toPattern(plan));
        }
        return ValidationContext.OK;
    }
    //endregion

    //region Private Methods

    /*
     * "Complete Join Op" - on the left branch of the JoinOp we are looking
     * for the last EntityOp(EOP) or EOP + attached EntityFilterOp (EFO).
     * We should check that we have this EOP (or EOP + EFO) at the right branch of the JoinOp.
     * i.e., The enums should be the same.
     */
    private boolean isJoinOpComplete(JoinOp joinOp) {
        Optional<EntityOp> leftEntityOp = PlanUtil.last(joinOp.getLeftBranch(), EntityOp.class);
        if (!leftEntityOp.isPresent()) {
            return false;
        }

        Optional<EntityOp> rightEntityOp = PlanUtil.<EntityOp>first(joinOp.getRightBranch(),
                op -> (op.geteNum() == leftEntityOp.get().geteNum()));

        if (!rightEntityOp.isPresent()) {
            return false;
        }

        Optional<EntityFilterOp> leftEntityFilterOp = PlanUtil.next(joinOp.getLeftBranch(), leftEntityOp.get(), EntityFilterOp.class);
        if (leftEntityFilterOp.isPresent()) {
            Optional<EntityFilterOp> rightEntityFilterOp = PlanUtil.next(joinOp.getRightBranch(), rightEntityOp.get(), EntityFilterOp.class);
            if (!rightEntityFilterOp.isPresent() ||
                    rightEntityFilterOp.get().geteNum() != leftEntityFilterOp.get().geteNum()) {
                return false;
            }
        }
        return true;
    }

    //endregion

}
