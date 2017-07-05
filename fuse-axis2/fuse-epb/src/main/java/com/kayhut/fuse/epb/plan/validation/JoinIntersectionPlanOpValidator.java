package com.kayhut.fuse.epb.plan.validation;

import com.kayhut.fuse.dispatcher.utils.PlanUtil;
import com.kayhut.fuse.dispatcher.utils.ValidationContext;
import com.kayhut.fuse.epb.plan.PlanValidator;
import com.kayhut.fuse.model.asgQuery.AsgQuery;
import com.kayhut.fuse.model.execution.plan.*;
import com.kayhut.fuse.model.log.Trace;
import javaslang.Tuple2;
import javaslang.collection.Stream;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.logging.Level;

import static com.kayhut.fuse.model.execution.plan.Plan.toPattern;

/**
 * Created by benishue on 7/5/2017.
 */
public class JoinIntersectionPlanOpValidator implements PlanValidator<Plan, AsgQuery> {
    private Trace<String> trace = Trace.build(JoinIntersectionPlanOpValidator.class.getSimpleName());

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
        We need to check validity only if the # of Ops is exactly 1 and the Op is JoinOp,
        if so, we need to check that the intersection of the Join branches (left, right)
        is 0 or 1.
        */
        if (plan.getOps().size() == 1 && joinOp.isPresent() && !isIntersectionValid(joinOp.get())) {
            return new ValidationContext(false, "JoinOp intersection validation failed: " + toPattern(plan));
        }
        return ValidationContext.OK;
    }
    //endregion

    //region Private Methods

    private boolean isIntersectionValid(JoinOp joinOp) {
        Set<Integer> leftEopSet = getEntityOpsRecursively(joinOp.getLeftBranch().getOps(), new HashSet<>());
        Set<Integer> rightEopSet = getEntityOpsRecursively(joinOp.getRightBranch().getOps(), new HashSet<>());

        Set<Integer> intersection = new HashSet<>(leftEopSet);
        intersection.retainAll(rightEopSet);

        //0 intersection is OK, since we can be in a state where we didn't finish yet.
        if (intersection.size() == 0) {
            return true;
        }
        if (intersection.size() == 1) {
            return (joinOp.getAsgEBase().geteNum() == intersection.iterator().next());
        }

        return false;
    }

    private Set<Integer> getEntityOpsRecursively(List<PlanOpBase> ops, Set<Integer> set) {
        for (PlanOpBase op : ops) {
            if (op instanceof EntityOp) {
                set.add(op.geteNum());
            }
            if (op instanceof JoinOp) {
                getEntityOpsRecursively(((JoinOp) op).getLeftBranch().getOps(), set);
                getEntityOpsRecursively(((JoinOp) op).getRightBranch().getOps(), set);
            }
        }
        return set;
    }

    //endregion

}
