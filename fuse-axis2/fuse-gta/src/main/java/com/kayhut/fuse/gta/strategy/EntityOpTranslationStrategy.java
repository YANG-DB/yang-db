package com.kayhut.fuse.gta.strategy;

import com.kayhut.fuse.gta.translation.PlanUtil;
import com.kayhut.fuse.model.execution.plan.EntityOp;
import com.kayhut.fuse.model.execution.plan.PlanOpBase;
import com.kayhut.fuse.model.execution.plan.RelationOp;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.structure.Graph;

import java.util.Optional;

/**
 * Created by Roman on 10/05/2017.
 */
public class EntityOpTranslationStrategy implements TranslationStrategy {
    //region Constructors
    public EntityOpTranslationStrategy(Graph graph) {
        this.graph = graph;
    }
    //endregion

    //region TranslationStrategy Implementation
    @Override
    public GraphTraversal apply(TranslationStrategyContext context, GraphTraversal traversal) {
        if (!(context.getPlanOp() instanceof EntityOp)) {
            return traversal;
        }

        EntityOp entityOp = (EntityOp)context.getPlanOp();

        if (PlanUtil.isFirst(context.getPlan(), context.getPlanOp())) {
            return graph.traversal().V().as(entityOp.getAsgEBase().geteBase().geteTag());
        } else {
            Optional<PlanOpBase> previousPlanOp = PlanUtil.getPrev(context.getPlan(), context.getPlanOp());
            if (previousPlanOp.isPresent() && previousPlanOp.get() instanceof RelationOp) {
                return traversal.otherV().as(entityOp.getAsgEBase().geteBase().geteTag());
            }
        }

        return traversal;
    }
    //endregion

    //region Fields
    private Graph graph;
    //endregion
}
