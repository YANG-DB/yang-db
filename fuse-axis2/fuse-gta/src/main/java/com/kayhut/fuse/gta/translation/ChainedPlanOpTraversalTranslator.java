package com.kayhut.fuse.gta.translation;

import com.google.inject.Inject;
import com.kayhut.fuse.gta.strategy.*;
import com.kayhut.fuse.model.execution.plan.*;
import com.kayhut.fuse.model.execution.plan.costs.PlanDetailedCost;
import org.apache.tinkerpop.gremlin.process.traversal.Path;
import org.apache.tinkerpop.gremlin.process.traversal.Traversal;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__;
import org.apache.tinkerpop.gremlin.structure.Element;

/**
 * Created by moti on 3/7/2017.
 */
public class ChainedPlanOpTraversalTranslator implements PlanTraversalTranslator {
    //region Constructors
    @Inject
    public ChainedPlanOpTraversalTranslator(PlanOpTranslationStrategy translationStrategy) {
        this.translationStrategy = translationStrategy;
        this.startFrom = 0;
    }

    public ChainedPlanOpTraversalTranslator(PlanOpTranslationStrategy translationStrategy, int startFrom) {
        this.translationStrategy = translationStrategy;
        this.startFrom = startFrom;
    }
    //endregion

    //region PlanTraversalTranslator Implementation
    public GraphTraversal<?, ?> translate(PlanWithCost<Plan, PlanDetailedCost> planWithCost, TranslationContext context) {
        GraphTraversal traversal = __.start();
        for (int planOpIndex = this.startFrom; planOpIndex < planWithCost.getPlan().getOps().size(); planOpIndex++) {
            traversal = this.translationStrategy.translate(traversal, planWithCost, planWithCost.getPlan().getOps().get(planOpIndex), context);
        }

        return traversal;
    }
    //endregion

    //region Fields
    private PlanOpTranslationStrategy translationStrategy;
    private int startFrom;
    //endregion
}
