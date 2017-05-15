package com.kayhut.fuse.gta.strategy;

import com.kayhut.fuse.gta.translation.TranslationContext;
import com.kayhut.fuse.model.execution.plan.GoToEntityOp;
import com.kayhut.fuse.model.execution.plan.Plan;
import com.kayhut.fuse.model.execution.plan.PlanOpBase;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;

/**
 * Created by Roman on 09/05/2017.
 */
public class GoToEntityOpTranslationStrategy implements PlanOpTranslationStrategy {
    //region PlanOpTranslationStrategy Implementation
    @Override
    public GraphTraversal translate(GraphTraversal traversal, Plan plan, PlanOpBase planOp, TranslationContext context) {
        if (planOp instanceof GoToEntityOp) {
            traversal.select(((GoToEntityOp)planOp).getAsgEBase().geteBase().geteTag());
        }

        return traversal;
    }
    //endregion
}
