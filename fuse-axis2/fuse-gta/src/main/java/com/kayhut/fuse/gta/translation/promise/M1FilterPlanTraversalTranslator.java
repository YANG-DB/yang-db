package com.kayhut.fuse.gta.translation.promise;

import com.kayhut.fuse.gta.strategy.promise.M1FilterPlanOpTranslationStrategy;
import com.kayhut.fuse.gta.translation.ChainedPlanOpTraversalTranslator;
import com.kayhut.fuse.dispatcher.gta.TranslationContext;
import com.kayhut.fuse.model.execution.plan.composite.Plan;
import com.kayhut.fuse.model.execution.plan.PlanWithCost;
import com.kayhut.fuse.model.execution.plan.costs.PlanDetailedCost;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;

/**
 * Created by Roman on 28/06/2017.
 */
public class M1FilterPlanTraversalTranslator extends ChainedPlanOpTraversalTranslator {
    //region Constructors
    public M1FilterPlanTraversalTranslator() {
        super(new M1FilterPlanOpTranslationStrategy());
    }
    //endregion

    //region Override Methods
    @Override
    public GraphTraversal<?, ?> translate(PlanWithCost<Plan, PlanDetailedCost> plan, TranslationContext context) {
        return super.translate(plan, context);
    }
    //endregion
}
