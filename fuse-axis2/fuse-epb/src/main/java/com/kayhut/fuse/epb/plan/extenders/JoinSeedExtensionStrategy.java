package com.kayhut.fuse.epb.plan.extenders;

import com.kayhut.fuse.dispatcher.epb.PlanExtensionStrategy;
import com.kayhut.fuse.model.asgQuery.AsgQuery;
import com.kayhut.fuse.model.execution.plan.composite.Plan;
import com.kayhut.fuse.model.execution.plan.entity.EntityJoinOp;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Created by moti on 7/3/2017.
 */
public class JoinSeedExtensionStrategy implements PlanExtensionStrategy<Plan, AsgQuery> {
    private PlanExtensionStrategy<Plan, AsgQuery> seedStrategy;

    public JoinSeedExtensionStrategy(PlanExtensionStrategy<Plan, AsgQuery> seedStrategy) {
        this.seedStrategy = seedStrategy;
    }

    @Override
    public Iterable<Plan> extendPlan(Optional<Plan> plan, AsgQuery query) {
        if (!plan.isPresent() || plan.get().getOps().isEmpty()) {
            return Collections.emptyList();
        }

        Iterable<Plan> plans = seedStrategy.extendPlan(Optional.empty(), query);

        List<Plan> newPlans = new ArrayList<>();
        for (Plan innerPlan : plans) {
            Plan leftBranch = Plan.clone(plan.get());
            EntityJoinOp join = new EntityJoinOp(leftBranch, innerPlan);
            newPlans.add(new Plan(join));
        }
        return newPlans;
    }
}
