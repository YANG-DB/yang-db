package com.kayhut.fuse.epb.plan.extenders;

import com.kayhut.fuse.epb.plan.PlanExtensionStrategy;
import com.kayhut.fuse.model.asgQuery.AsgQuery;
import com.kayhut.fuse.model.execution.plan.EntityJoinOp;
import com.kayhut.fuse.model.execution.plan.Plan;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Created by moti on 7/3/2017.
 */
public class JoinOngoingExtensionStrategy implements PlanExtensionStrategy<Plan, AsgQuery> {
    private PlanExtensionStrategy<Plan, AsgQuery> innerExpander;

    public JoinOngoingExtensionStrategy(PlanExtensionStrategy<Plan, AsgQuery> innerExpander) {
        this.innerExpander = innerExpander;
    }

    @Override
    public Iterable<Plan> extendPlan(Optional<Plan> plan, AsgQuery query) {
        if(!plan.isPresent())
            return Collections.emptyList();
        if (plan.get().getOps().isEmpty()) {
            return Collections.emptyList();
        }

        if (plan.get().getOps().size() == 1 && plan.get().getOps().get(0) instanceof EntityJoinOp) {
            EntityJoinOp joinOp = (EntityJoinOp) plan.get().getOps().get(0);
            Iterable<Plan> plans = innerExpander.extendPlan(Optional.of(joinOp.getRightBranch()), query);
            List<Plan> newPlans = new ArrayList<>();
            for (Plan innerPlan : plans) {
                newPlans.add(new Plan(new EntityJoinOp(joinOp.getLeftBranch(), innerPlan)));
            }
            return newPlans;
        }

        return Collections.emptyList();
    }
}
