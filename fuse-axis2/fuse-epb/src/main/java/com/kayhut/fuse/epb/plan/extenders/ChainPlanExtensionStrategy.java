package com.kayhut.fuse.epb.plan.extenders;

import com.google.inject.Inject;
import com.kayhut.fuse.epb.plan.PlanExtensionStrategy;
import com.kayhut.fuse.model.asgQuery.AsgQuery;
import com.kayhut.fuse.model.execution.plan.Plan;

import java.util.Collections;
import java.util.Optional;

public class ChainPlanExtensionStrategy implements PlanExtensionStrategy<Plan, AsgQuery> {

    private PlanExtensionStrategy<Plan, AsgQuery>[] innerExtenders;

    @Inject
    public ChainPlanExtensionStrategy(PlanExtensionStrategy<Plan, AsgQuery> ... innerExtenders) {
        this.innerExtenders = innerExtenders;
    }

    @Override
    public Iterable<Plan> extendPlan(Optional<Plan> plan, AsgQuery query) {
        Plan myPlan = plan.get();
        for(PlanExtensionStrategy<Plan, AsgQuery> extensionStrategy : innerExtenders){
            Iterable<Plan> ps = extensionStrategy.extendPlan(plan, query);
            for (Plan p : ps) {
                myPlan = Plan.compose(myPlan,p);
            }
        }
        return Collections.singletonList(myPlan);
    }
}
