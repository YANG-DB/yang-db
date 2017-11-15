package com.kayhut.fuse.gta.strategy.common;

import com.kayhut.fuse.gta.strategy.PlanOpTranslationStrategyBase;
import com.kayhut.fuse.gta.translation.TranslationContext;
import com.kayhut.fuse.model.execution.plan.GoToEntityOp;
import com.kayhut.fuse.model.execution.plan.Plan;
import com.kayhut.fuse.model.execution.plan.PlanOpBase;
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
    protected GraphTraversal translateImpl(GraphTraversal traversal, PlanWithCost<Plan, PlanDetailedCost> plan, PlanOpBase planOp, TranslationContext context) {
        return traversal.select(((GoToEntityOp)planOp).getAsgEBase().geteBase().geteTag());
    }
    //endregion
}
