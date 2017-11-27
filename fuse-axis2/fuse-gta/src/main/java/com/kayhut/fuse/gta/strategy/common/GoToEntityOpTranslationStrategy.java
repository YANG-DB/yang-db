package com.kayhut.fuse.gta.strategy.common;

import com.kayhut.fuse.gta.strategy.PlanOpTranslationStrategyBase;
import com.kayhut.fuse.gta.translation.TranslationContext;
import com.kayhut.fuse.model.execution.plan.entity.GoToEntityOp;
import com.kayhut.fuse.model.execution.plan.composite.Plan;
import com.kayhut.fuse.model.execution.plan.PlanOp;
import com.kayhut.fuse.model.execution.plan.PlanWithCost;
import com.kayhut.fuse.model.execution.plan.costs.PlanDetailedCost;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;

/**
 * Created by Roman on 09/05/2017.
 */
public class GoToEntityOpTranslationStrategy extends PlanOpTranslationStrategyBase {
    //region Constructors
    //endregion
    public GoToEntityOpTranslationStrategy() {
        super(GoToEntityOp.class);
    }

    //region PlanOpTranslationStrategy Implementation
    @Override
    protected GraphTraversal translateImpl(GraphTraversal traversal, PlanWithCost<Plan, PlanDetailedCost> plan, PlanOp planOp, TranslationContext context) {
        return traversal.select(((GoToEntityOp)planOp).getAsgEbase().geteBase().geteTag());
    }
    //endregion
}
