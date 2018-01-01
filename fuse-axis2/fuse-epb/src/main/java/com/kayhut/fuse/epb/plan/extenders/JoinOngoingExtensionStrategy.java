package com.kayhut.fuse.epb.plan.extenders;

import com.kayhut.fuse.dispatcher.epb.PlanExtensionStrategy;
import com.kayhut.fuse.dispatcher.utils.PlanUtil;
import com.kayhut.fuse.model.asgQuery.AsgQuery;
import com.kayhut.fuse.model.execution.plan.composite.Plan;
import com.kayhut.fuse.model.execution.plan.entity.EntityJoinOp;
import javaslang.collection.Stream;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Created by moti on 7/3/2017.
 * Extends join ops that are the last op in the plan
 */
public class JoinOngoingExtensionStrategy implements PlanExtensionStrategy<Plan, AsgQuery> {
    private PlanExtensionStrategy<Plan, AsgQuery> innerExpander;

    public JoinOngoingExtensionStrategy(PlanExtensionStrategy<Plan, AsgQuery> innerExpander) {
        this.innerExpander = innerExpander;
    }

    @Override
    public Iterable<Plan> extendPlan(Optional<Plan> plan, AsgQuery query) {
        // Cannot continue a join from an empty plan
        if(!plan.isPresent() || plan.get().getOps().isEmpty()) {
            return Collections.emptyList();
        }

        // Check if the plan has a Join op and this is the last op
        if (plan.get().getOps().size() == 1 && PlanUtil.last(plan.get(), EntityJoinOp.class).isPresent()) {
            EntityJoinOp joinOp = PlanUtil.last(plan.get(), EntityJoinOp.class).get();
            //TODO: maybe this if should not be here
            if(joinOp.isComplete()) {
                // Can't continue a join which is complete
                return Collections.emptyList();
            }

            // extend right branch and create new plans
            return Stream.ofAll(innerExpander.extendPlan(Optional.of(joinOp.getRightBranch()), query))
                    .map(extendedRightBranch -> new Plan(new EntityJoinOp(joinOp.getLeftBranch(), extendedRightBranch)))
                    .toJavaList();
        }

        return Collections.emptyList();
    }
}
