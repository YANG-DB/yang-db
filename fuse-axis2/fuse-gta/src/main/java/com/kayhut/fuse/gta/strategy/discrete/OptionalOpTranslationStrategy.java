package com.kayhut.fuse.gta.strategy.discrete;

import com.kayhut.fuse.dispatcher.utils.PlanUtil;
import com.kayhut.fuse.gta.strategy.PlanOpTranslationStrategy;
import com.kayhut.fuse.gta.strategy.PlanOpTranslationStrategyBase;
import com.kayhut.fuse.gta.strategy.common.CompositePlanOpTranslationStrategy;
import com.kayhut.fuse.gta.translation.ChainedPlanOpTraversalTranslator;
import com.kayhut.fuse.gta.translation.PlanTraversalTranslator;
import com.kayhut.fuse.gta.translation.TranslationContext;
import com.kayhut.fuse.model.execution.plan.OptionalOp;
import com.kayhut.fuse.model.execution.plan.Plan;
import com.kayhut.fuse.model.execution.plan.PlanOpBase;
import com.kayhut.fuse.model.execution.plan.PlanWithCost;
import com.kayhut.fuse.model.execution.plan.costs.PlanDetailedCost;
import javaslang.collection.Stream;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;

import java.util.Optional;

/**
 * Created by roman.margolis on 20/11/2017.
 */
public class OptionalOpTranslationStrategy extends PlanOpTranslationStrategyBase {
    //region Constructors
    public OptionalOpTranslationStrategy(PlanOpTranslationStrategy planOpTranslationStrategy) {
        super(OptionalOp.class);
        this.planOpTranslationStrategy = planOpTranslationStrategy;
    }
    //endregion

    //region PlanOpTranslationStrategyBase Implementation
    @Override
    protected GraphTraversal<?, ?> translateImpl(GraphTraversal traversal, PlanWithCost<Plan, PlanDetailedCost> planWithCost, PlanOpBase planOp, TranslationContext context) {
        OptionalOp optionalOp = (OptionalOp)planOp;

        // take all the ops preceding the optional op - this is necessary for proper context for the plan op translation strategies.
        int indexOfOptional = planWithCost.getPlan().getOps().indexOf(planOp);
        Plan optionalPlan = new Plan(Stream.ofAll(planWithCost.getPlan().getOps()).take(indexOfOptional).toJavaList()).append(optionalOp);

        PlanTraversalTranslator planTraversalTranslator =
                new ChainedPlanOpTraversalTranslator(this.planOpTranslationStrategy, indexOfOptional);

        GraphTraversal<?, ?> optionalTraversal = planTraversalTranslator.translate(new PlanWithCost<>(optionalPlan, planWithCost.getCost()), context);
        return traversal.optional(optionalTraversal);
    }
    //endregion

    //region Fields
    private PlanOpTranslationStrategy planOpTranslationStrategy;
    //endregion
}
