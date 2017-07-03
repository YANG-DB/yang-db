package com.kayhut.fuse.epb.plan.extenders;

import com.google.inject.Inject;
import com.kayhut.fuse.epb.plan.PlanExtensionStrategy;
import com.kayhut.fuse.model.asgQuery.AsgQuery;
import com.kayhut.fuse.model.execution.plan.JoinOp;
import com.kayhut.fuse.model.execution.plan.Plan;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Created by moti on 7/3/2017.
 */
public class JoinSeedExtensionStrategy implements PlanExtensionStrategy<Plan, AsgQuery> {
    private PlanExtensionStrategy<Plan, AsgQuery> innerExpander;

    public JoinSeedExtensionStrategy(PlanExtensionStrategy<Plan, AsgQuery> innerExpander) {
        this.innerExpander = innerExpander;
    }

    @Override
    public Iterable<Plan> extendPlan(Optional<Plan> plan, AsgQuery query) {
        if(!plan.isPresent())
            return Collections.emptyList();

        Iterable<Plan> plans = innerExpander.extendPlan(Optional.empty(), query);

        List<Plan> newPlans = new ArrayList<>();
        for (Plan innerPlan : plans) {
            Plan leftBranch = Plan.clone(plan.get());
            JoinOp join = new JoinOp(leftBranch, innerPlan);
            newPlans.add(new Plan(join));
        }

        return newPlans;
    }
}
