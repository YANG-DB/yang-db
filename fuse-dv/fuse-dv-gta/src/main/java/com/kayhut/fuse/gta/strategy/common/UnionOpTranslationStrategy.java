package com.kayhut.fuse.gta.strategy.common;

import com.kayhut.fuse.dispatcher.gta.PlanTraversalTranslator;
import com.kayhut.fuse.dispatcher.gta.TranslationContext;
import com.kayhut.fuse.gta.strategy.PlanOpTranslationStrategy;
import com.kayhut.fuse.gta.strategy.PlanOpTranslationStrategyBase;
import com.kayhut.fuse.gta.translation.ChainedPlanOpTraversalTranslator;
import com.kayhut.fuse.model.execution.plan.PlanOp;
import com.kayhut.fuse.model.execution.plan.PlanWithCost;
import com.kayhut.fuse.model.execution.plan.composite.OptionalOp;
import com.kayhut.fuse.model.execution.plan.composite.Plan;
import com.kayhut.fuse.model.execution.plan.composite.UnionOp;
import com.kayhut.fuse.model.execution.plan.costs.PlanDetailedCost;
import javaslang.collection.Stream;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;

import java.util.List;

public class UnionOpTranslationStrategy extends PlanOpTranslationStrategyBase {
    private final PlanOpTranslationStrategy planOpTranslationStrategy;

    public UnionOpTranslationStrategy(PlanOpTranslationStrategy planOpTranslationStrategy) {
        super(planOp -> planOp.getClass().equals(UnionOp.class));
        this.planOpTranslationStrategy = planOpTranslationStrategy;
    }

    @Override
    protected GraphTraversal translateImpl(GraphTraversal traversal, PlanWithCost<Plan, PlanDetailedCost> planWithCost, PlanOp planOp, TranslationContext context) {
        UnionOp unionOp = (UnionOp)planOp;

        int indexOfUnion = planWithCost.getPlan().getOps().indexOf(planOp);
        final List<? extends GraphTraversal<?, ?>> traversalList = Stream.ofAll(unionOp.getPlans()).map(plan -> {
            //create a (one of) branch union plan
            Plan unionPlan = new Plan(Stream.ofAll(planWithCost.getPlan().getOps()).take(indexOfUnion).toJavaList()).append(plan);
            //create chained plan translator
            PlanTraversalTranslator planTraversalTranslator = new ChainedPlanOpTraversalTranslator(this.planOpTranslationStrategy, indexOfUnion);
            //return the gremlin translated plan
            return planTraversalTranslator.translate(new PlanWithCost<>(unionPlan, planWithCost.getCost()), context);
        }).toJavaList();
        //traversal union translated branches
        return traversal.union(traversalList.toArray(new GraphTraversal[traversalList.size()]));
    }
}
