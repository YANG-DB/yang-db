package com.kayhut.fuse.epb.plan.validation;

import com.kayhut.fuse.dispatcher.utils.PlanUtil;
import com.kayhut.fuse.dispatcher.utils.ValidationContext;
import com.kayhut.fuse.epb.plan.PlanValidator;
import com.kayhut.fuse.model.asgQuery.AsgQuery;
import com.kayhut.fuse.model.execution.plan.*;
import com.kayhut.fuse.model.log.Trace;
import javaslang.Tuple2;

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.logging.Level;

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
        if (plan.getOps().size() > 1 && joinOp.isPresent() && PlanUtil.isFirst(plan, joinOp.get())
                && !isJoinOpComplete(joinOp.get())) {
            return new ValidationContext(false, "JoinOp Validation failed on");
        }
        return ValidationContext.OK;
    }

    private boolean isJoinOpComplete(JoinOp joinOp) {
        EntityOp leftEntityOp = PlanUtil.last$(joinOp.getLeftBranch(), EntityOp.class);
        Optional<EntityOp> rightEntityOp = PlanUtil.<EntityOp>first(joinOp.getRightBranch(),
                op -> (op.geteNum() == leftEntityOp.geteNum()));

        if (!rightEntityOp.isPresent()) {
            return false;
        }
        Optional<PlanOpBase> leftEntityFilterOp = PlanUtil.adjacentNext(joinOp.getLeftBranch(), leftEntityOp);
        if (leftEntityFilterOp.isPresent() && leftEntityFilterOp.get() instanceof EntityFilterOp) {
            Optional<PlanOpBase> rightEntityFilterOp = PlanUtil.adjacentNext(joinOp.getRightBranch(), rightEntityOp.get());
            if (!rightEntityFilterOp.isPresent() ||
                    rightEntityFilterOp.get().geteNum() != leftEntityFilterOp.get().geteNum()) {
                return false;
            }
        }
        return true;
    }

    //endregion
}
