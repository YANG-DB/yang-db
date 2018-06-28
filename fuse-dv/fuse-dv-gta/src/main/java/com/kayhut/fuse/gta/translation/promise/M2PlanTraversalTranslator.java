package com.kayhut.fuse.gta.translation.promise;

import com.kayhut.fuse.dispatcher.gta.TranslationContext;
import com.kayhut.fuse.gta.strategy.promise.M1PlanOpTranslationStrategy;
import com.kayhut.fuse.gta.strategy.promise.M2PlanOpTranslationStrategy;
import com.kayhut.fuse.gta.translation.ChainedPlanOpTraversalTranslator;
import com.kayhut.fuse.model.execution.plan.PlanWithCost;
import com.kayhut.fuse.model.execution.plan.composite.Plan;
import com.kayhut.fuse.model.execution.plan.costs.PlanDetailedCost;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;

/**
 * Created by Roman on 28/06/2017.
 */
public class M2PlanTraversalTranslator extends ChainedPlanOpTraversalTranslator {
    //region Constructors
    public M2PlanTraversalTranslator() {
        super(new M2PlanOpTranslationStrategy());
    }
    //endregion

    //region Override Methods
    @Override
    public GraphTraversal<?, ?> translate(PlanWithCost<Plan, PlanDetailedCost> planWithCost, TranslationContext context){
        return super.translate(planWithCost, context);
    }
    //endregion
}
