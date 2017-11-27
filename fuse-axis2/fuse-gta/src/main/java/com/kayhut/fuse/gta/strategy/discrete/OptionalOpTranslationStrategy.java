package com.kayhut.fuse.gta.strategy.discrete;

import com.kayhut.fuse.gta.strategy.PlanOpTranslationStrategy;
import com.kayhut.fuse.gta.strategy.PlanOpTranslationStrategyBase;
import com.kayhut.fuse.gta.translation.ChainedPlanOpTraversalTranslator;
import com.kayhut.fuse.gta.translation.PlanTraversalTranslator;
import com.kayhut.fuse.gta.translation.TranslationContext;
import com.kayhut.fuse.model.execution.plan.composite.OptionalOp;
import com.kayhut.fuse.model.execution.plan.composite.Plan;
import com.kayhut.fuse.model.execution.plan.PlanOp;
import com.kayhut.fuse.model.execution.plan.PlanWithCost;
import com.kayhut.fuse.model.execution.plan.costs.PlanDetailedCost;
import javaslang.collection.Stream;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;

/**
 * Created by roman.margolis on 20/11/2017.
 */
public class OptionalOpTranslationStrategy extends PlanOpTranslationStrategyBase {
    //region Constructors
    public OptionalOpTranslationStrategy(PlanOpTranslationStrategy planOpTranslationStrategy) {
        super(planOp -> planOp.getClass().equals(OptionalOp.class));
        this.planOpTranslationStrategy = planOpTranslationStrategy;
    }
    //endregion

    //region PlanOpTranslationStrategyBase Implementation
    @Override
    protected GraphTraversal<?, ?> translateImpl(GraphTraversal traversal, PlanWithCost<Plan, PlanDetailedCost> planWithCost, PlanOp planOp, TranslationContext context) {
        OptionalOp optionalOp = (OptionalOp)planOp;

        int indexOfOptional = planWithCost.getPlan().getOps().indexOf(planOp);
        //Plan optionalPlan = new Plan(Stream.ofAll(planWithCost.getPlan().getOps()).take(indexOfOptional).toJavaList()).append(optionalOp);

        PlanTraversalTranslator planTraversalTranslator =
                new ChainedPlanOpTraversalTranslator(this.planOpTranslationStrategy, indexOfOptional);

        GraphTraversal<?, ?> optionalTraversal = planTraversalTranslator.translate(new PlanWithCost<>(planWithCost.getPlan(), planWithCost.getCost()), context);
        return traversal.optional(optionalTraversal);
    }
    //endregion

    //region Fields
    private PlanOpTranslationStrategy planOpTranslationStrategy;
    //endregion
}
