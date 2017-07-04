package com.kayhut.fuse.epb.plan.extenders;

import com.kayhut.fuse.epb.plan.PlanExtensionStrategy;
import com.kayhut.fuse.epb.plan.PlanSeedStrategy;
import com.kayhut.fuse.model.asgQuery.AsgQuery;
import com.kayhut.fuse.model.execution.plan.JoinOp;
import com.kayhut.fuse.model.execution.plan.Plan;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by moti on 7/3/2017.
 */
public class JoinSeedExtensionStrategy implements PlanExtensionStrategy<Plan, AsgQuery> {
    private PlanSeedStrategy<Plan, AsgQuery> seedStrategy;
    private PlanExtensionStrategy<Plan, AsgQuery> innerExpander;

    public JoinSeedExtensionStrategy(PlanSeedStrategy<Plan, AsgQuery> seedStrategy) {
        this.seedStrategy = seedStrategy;
    }

    @Override
    public Iterable<Plan> extendPlan(Plan plan, AsgQuery query) {
        Iterable<Plan> plans = seedStrategy.extendPlan(query);

        List<Plan> newPlans = new ArrayList<>();
        for (Plan innerPlan : plans) {
            Plan leftBranch = Plan.clone(plan);
            JoinOp join = new JoinOp(leftBranch, innerPlan);
            newPlans.add(new Plan(join));
        }

        return newPlans;
    }
}
