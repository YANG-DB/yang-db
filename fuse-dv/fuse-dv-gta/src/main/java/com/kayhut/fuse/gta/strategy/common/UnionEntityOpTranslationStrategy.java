package com.kayhut.fuse.gta.strategy.common;

import com.kayhut.fuse.dispatcher.gta.PlanTraversalTranslator;
import com.kayhut.fuse.dispatcher.gta.TranslationContext;
import com.kayhut.fuse.gta.strategy.PlanOpTranslationStrategyBase;
import com.kayhut.fuse.model.execution.plan.PlanOp;
import com.kayhut.fuse.model.execution.plan.PlanWithCost;
import com.kayhut.fuse.model.execution.plan.composite.Plan;
import com.kayhut.fuse.model.execution.plan.costs.CountEstimatesCost;
import com.kayhut.fuse.model.execution.plan.costs.PlanDetailedCost;
import com.kayhut.fuse.model.execution.plan.costs.UnionCost;
import com.kayhut.fuse.model.execution.plan.entity.UnionJoinOp;
import javaslang.collection.Stream;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;

import java.util.Optional;
import java.util.function.Predicate;

public class UnionEntityOpTranslationStrategy extends PlanOpTranslationStrategyBase {
    PlanTraversalTranslator planTraversalTranslator;

    @SafeVarargs
    public UnionEntityOpTranslationStrategy(PlanTraversalTranslator planTraversalTranslator, Class<? extends PlanOp>... klasses) {
        super(klasses);
        this.planTraversalTranslator = planTraversalTranslator;
    }

    public UnionEntityOpTranslationStrategy(Predicate<PlanOp> planOpPredicate, PlanTraversalTranslator planTraversalTranslator) {
        super(planOpPredicate);
        this.planTraversalTranslator = planTraversalTranslator;
    }

    @Override
    protected GraphTraversal translateImpl(GraphTraversal traversal, PlanWithCost<Plan, PlanDetailedCost> plan, PlanOp planOp, TranslationContext context) {
        if(planOp instanceof UnionJoinOp){
            UnionCost joinCost = (UnionCost) plan.getCost().getPlanStepCost(planOp).get().getCost();
            CountEstimatesCost commonCost = Stream.ofAll(joinCost.getCommonPlanCost().getPlanStepCosts()).last().getCost();
            final Optional<CountEstimatesCost> unionCost = joinCost.getBranchesCosts().stream().map(p -> Stream.ofAll(p.getPlanStepCosts()).last().getCost())
                    .reduce((a, b) -> new CountEstimatesCost(a.getCost() + b.getCost(), a.getCost() + b.getCost()));


//            GraphTraversal<?, ?> optionalTraversal = planTraversalTranslator.translate(new PlanWithCost<>(optionalPlan, planWithCost.getCost()), context);
//            return traversal.optional(optionalTraversal);
        }
        return traversal;
    }
}
