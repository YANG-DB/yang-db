package com.kayhut.fuse.epb.plan.extenders;

import com.google.inject.Inject;
import com.kayhut.fuse.epb.plan.PlanExtensionStrategy;
import com.kayhut.fuse.model.asgQuery.AsgQuery;
import com.kayhut.fuse.model.execution.plan.Plan;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class ChainPlanExtensionStrategy implements PlanExtensionStrategy<Plan, AsgQuery> {

    private PlanExtensionStrategy<Plan, AsgQuery>[] innerExtenders;
    private Level level;

    @Inject
    public ChainPlanExtensionStrategy(PlanExtensionStrategy<Plan, AsgQuery>... innerExtenders ) {
        this.innerExtenders = innerExtenders;
        this.level = level;
    }

    @Override
    public Iterable<Plan> extendPlan(Optional<Plan> plan, AsgQuery query) {
        Plan myPlan = plan.get();
        Collection<Plan> plans = new ArrayList<>();
        plans.add(myPlan);

        for (PlanExtensionStrategy<Plan, AsgQuery> extensionStrategy : innerExtenders) {
            List<Plan> collect = plans.stream()
                    .map(p -> extensionStrategy.extendPlan(Optional.of(p), query))
                    .flatMap(t -> StreamSupport.stream(t.spliterator(), false))
                    .collect(Collectors.toList());
            plans = collect;
        }
        return plans;
    }
}
