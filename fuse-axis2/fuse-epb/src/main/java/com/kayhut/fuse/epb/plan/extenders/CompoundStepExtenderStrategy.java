package com.kayhut.fuse.epb.plan.extenders;

import com.google.common.collect.Lists;
import com.kayhut.fuse.epb.plan.PlanExtensionStrategy;
import com.kayhut.fuse.model.asgQuery.AsgQuery;
import com.kayhut.fuse.model.execution.plan.Plan;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Created by liorp on 5/9/2017.
 * <p>
 * Compound (a special composite use case)
 * * InitialPlanGeneratorExtensionStrategy ( empty plan extender)
 *
 * * ancestorStrategy
 * * descendantStrategy
 * * chainStrategy
 *      * gotoStrategy
 *      * compositeStrategy
 *          * ancestorStrategy
 *          * descendantStrategy
 */
public class CompoundStepExtenderStrategy implements PlanExtensionStrategy<Plan, AsgQuery> {

    private StepAncestorAdjacentStrategy ancestorStrategy;
    private StepDescendantsAdjacentStrategy descendantStrategy;
    private ChainPlanExtensionStrategy chainStrategy;
    private InitialPlanGeneratorExtensionStrategy seedStrategy;

    public CompoundStepExtenderStrategy() {
        seedStrategy = new InitialPlanGeneratorExtensionStrategy();
        ancestorStrategy = new StepAncestorAdjacentStrategy();
        descendantStrategy = new StepDescendantsAdjacentStrategy();
        chainStrategy = new ChainPlanExtensionStrategy(new GotoExtensionStrategy(),
                new CompositePlanExtensionStrategy<>(ancestorStrategy, descendantStrategy));
    }

    @Override
    public Iterable<Plan> extendPlan(Optional<Plan> plan, AsgQuery query) {
        if (!plan.isPresent()) {
            return seedStrategy.extendPlan(plan,query);
        }

        List<Plan> plans = new ArrayList<>();
        ArrayList<Plan> ancestor = Lists.newArrayList(ancestorStrategy.extendPlan(plan, query));
        plans.addAll(ancestor);
        ArrayList<Plan> descendant = Lists.newArrayList(descendantStrategy.extendPlan(plan, query));
        plans.addAll(descendant);
        ArrayList<Plan> chain = Lists.newArrayList(chainStrategy.extendPlan(plan, query));
        plans.addAll(chain);
        return plans;
    }
}
