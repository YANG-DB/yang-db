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
 * Created by moti
 * Creates new Join ops, extends the left branch with a given extender (goto probably), and creates a cross product of
 * extended left branches and right branches (seed plans)
 */
public class JoinGotoSeedExtensionStrategy implements PlanExtensionStrategy<Plan, AsgQuery> {
    private PlanExtensionStrategy<Plan, AsgQuery> seedStrategy;
    private PlanExtensionStrategy<Plan, AsgQuery> gotoStrategy;

    public JoinGotoSeedExtensionStrategy(PlanExtensionStrategy<Plan, AsgQuery> seedStrategy, PlanExtensionStrategy<Plan, AsgQuery> gotoStrategy) {
        this.seedStrategy = seedStrategy;
        this.gotoStrategy = gotoStrategy;
    }

    @Override
    public Iterable<Plan> extendPlan(Optional<Plan> plan, AsgQuery query) {
        // Cannot create a new join from an empty plan
        if (!plan.isPresent() || plan.get().getOps().isEmpty()) {
            return Collections.emptyList();
        }
        List<Plan> newPlans = new ArrayList<>();

        Iterable<Plan> plans = seedStrategy.extendPlan(Optional.empty(), query);

        for (Plan gotoPlan : this.gotoStrategy.extendPlan(plan, query)) {
            for (Plan innerPlan : plans) {
                EntityJoinOp join = new EntityJoinOp(Plan.clone(gotoPlan), Plan.clone(innerPlan));
                newPlans.add(new Plan(join));
            }
        }

        return newPlans;
    }
}
