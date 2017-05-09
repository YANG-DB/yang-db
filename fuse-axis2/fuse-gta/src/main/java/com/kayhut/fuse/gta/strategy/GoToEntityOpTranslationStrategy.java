package com.kayhut.fuse.gta.strategy;

import com.kayhut.fuse.gta.translation.PlanUtil;
import com.kayhut.fuse.model.execution.plan.EntityOp;
import com.kayhut.fuse.model.execution.plan.GoToEntityOp;
import com.kayhut.fuse.model.execution.plan.Plan;
import com.kayhut.fuse.model.execution.plan.PlanOpBase;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;

import java.util.Optional;

/**
 * Created by Roman on 09/05/2017.
 */
public class GoToEntityOpTranslationStrategy implements TranslationStrategy {
    //region TranslationStrategy Implementation
    @Override
    public GraphTraversal apply(TranslationStrategyContext context, GraphTraversal traversal) {
        if (context.getPlanOp() instanceof GoToEntityOp) {
            traversal.select(((GoToEntityOp) context.getPlanOp()).getAsgEBase().geteBase().geteTag());
        }

        return traversal;
    }
    //endregion
}
