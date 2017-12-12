package com.kayhut.fuse.epb.plan.validation.opValidator;

import com.kayhut.fuse.dispatcher.epb.PlanValidator;
import com.kayhut.fuse.dispatcher.utils.PlanUtil;
import com.kayhut.fuse.dispatcher.utils.ValidationContext;
import com.kayhut.fuse.model.asgQuery.AsgQuery;
import com.kayhut.fuse.model.execution.plan.PlanOp;
import com.kayhut.fuse.model.execution.plan.composite.Plan;
import com.kayhut.fuse.model.execution.plan.composite.descriptors.IterablePlanOpDescriptor;
import com.kayhut.fuse.model.execution.plan.entity.EntityJoinOp;
import com.kayhut.fuse.model.execution.plan.entity.EntityOp;
import com.kayhut.fuse.model.log.Trace;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Created by benishue on 7/5/2017.
 */
public class JoinIntersectionPlanOpValidator implements PlanValidator<Plan, AsgQuery> {
    private Trace<String> trace = Trace.build(JoinIntersectionPlanOpValidator.class.getSimpleName());

    //region PlanValidator Implementation
    @Override
    public ValidationContext isPlanValid(Plan plan, AsgQuery query) {
        Optional<EntityJoinOp> joinOp = PlanUtil.first(plan, EntityJoinOp.class);
        /*
        We need to check validity only if the # of Ops is exactly 1 and the Op is JoinOp,
        if so, we need to check that the intersection of the Join branches (left, right)
        is 0 or 1.
        */
        if (plan.getOps().size() == 1 && joinOp.isPresent() && !isIntersectionValid(joinOp.get())) {
            return new ValidationContext(false, "JoinOp intersection validation failed: " + IterablePlanOpDescriptor.getSimple().describe(plan.getOps()));
        }
        return ValidationContext.OK;
    }
    //endregion

    //region Private Methods

    private boolean isIntersectionValid(EntityJoinOp joinOp) {
        Set<Integer> leftEopSet = getEntityOpsRecursively(joinOp.getLeftBranch().getOps(), new HashSet<>());
        Set<Integer> rightEopSet = getEntityOpsRecursively(joinOp.getRightBranch().getOps(), new HashSet<>());

        Set<Integer> intersection = new HashSet<>(leftEopSet);
        intersection.retainAll(rightEopSet);

        //0 intersection is OK, since we can be in a state where we didn't finish yet.
        if (intersection.size() == 0) {
            return true;
        }
        if (intersection.size() == 1) {
            return (joinOp.getAsgEbase().geteNum() == intersection.iterator().next());
        }

        return false;
    }

    private Set<Integer> getEntityOpsRecursively(List<PlanOp> ops, Set<Integer> set) {
        for (PlanOp op : ops) {
            if (op instanceof EntityOp) {
                set.add(((EntityOp)op).getAsgEbase().geteNum());
            }
            if (op instanceof EntityJoinOp) {
                getEntityOpsRecursively(((EntityJoinOp) op).getLeftBranch().getOps(), set);
                getEntityOpsRecursively(((EntityJoinOp) op).getRightBranch().getOps(), set);
            }
        }
        return set;
    }

    //endregion

}
