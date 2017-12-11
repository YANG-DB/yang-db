package com.kayhut.fuse.epb.plan.validation;

import com.kayhut.fuse.dispatcher.epb.PlanValidator;
import com.kayhut.fuse.dispatcher.utils.PlanUtil;
import com.kayhut.fuse.dispatcher.utils.ValidationContext;
import com.kayhut.fuse.model.asgQuery.AsgQuery;
import com.kayhut.fuse.model.execution.plan.composite.Plan;
import com.kayhut.fuse.model.execution.plan.composite.descriptors.IterablePlanOpDescriptor;
import com.kayhut.fuse.model.execution.plan.entity.EntityFilterOp;
import com.kayhut.fuse.model.execution.plan.entity.EntityJoinOp;
import com.kayhut.fuse.model.execution.plan.entity.EntityOp;
import com.kayhut.fuse.model.log.Trace;
import java.util.Optional;



/**
 * Created by benishue on 7/4/2017.
 */
public class JoinCompletePlanOpValidator implements PlanValidator<Plan, AsgQuery> {
    private Trace<String> trace = Trace.build(JoinCompletePlanOpValidator.class.getSimpleName());


    @Override
    public ValidationContext isPlanValid(Plan plan, AsgQuery query) {
        Optional<EntityJoinOp> joinOp = PlanUtil.first(plan, EntityJoinOp.class);
        /*
        We need to check validity only if the # of Ops is at least 2, if so, we need to check
        that this JoinOp is a 'Complete' one (See function below).
        The 'Extenders' (according to Mr. R) will always make sure that the JoinOp will be the first element.
        */
        if (plan.getOps().size() > 1 && joinOp.isPresent() && PlanUtil.isFirst(plan, joinOp.get())
                && !isJoinOpComplete(joinOp.get())) {
            return new ValidationContext(false, "JoinOp complete validation failed: " + IterablePlanOpDescriptor.getSimple().describe(plan.getOps()));
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
    private boolean isJoinOpComplete(EntityJoinOp joinOp) {
        Optional<EntityOp> leftEntityOp = PlanUtil.last(joinOp.getLeftBranch(), EntityOp.class);
        if (!leftEntityOp.isPresent()) {
            return false;
        }

        Optional<EntityOp> rightEntityOp = PlanUtil.<EntityOp>first(joinOp.getRightBranch(),
                op -> ((op instanceof EntityOp) && (((EntityOp)op).getAsgEbase().geteNum() == leftEntityOp.get().getAsgEbase().geteNum())));

        if (!rightEntityOp.isPresent()) {
            return false;
        }

        Optional<EntityFilterOp> leftEntityFilterOp = PlanUtil.next(joinOp.getLeftBranch(), leftEntityOp.get(), EntityFilterOp.class);
        if (leftEntityFilterOp.isPresent()) {
            Optional<EntityFilterOp> rightEntityFilterOp = PlanUtil.next(joinOp.getRightBranch(), rightEntityOp.get(), EntityFilterOp.class);
            if (!rightEntityFilterOp.isPresent() ||
                    rightEntityFilterOp.get().getAsgEbase().geteNum() != leftEntityFilterOp.get().getAsgEbase().geteNum()) {
                return false;
            }
        }
        return true;
    }

    //endregion

}
