package com.kayhut.fuse.gta.strategy.discrete;

import com.kayhut.fuse.gta.strategy.common.CompositePlanOpTranslationStrategy;
import com.kayhut.fuse.gta.strategy.common.EntityTranslationOptions;
import com.kayhut.fuse.gta.strategy.common.GoToEntityOpTranslationStrategy;
import com.kayhut.fuse.gta.translation.TranslationContext;
import com.kayhut.fuse.model.execution.plan.EntityFilterOp;
import com.kayhut.fuse.model.execution.plan.EntityOp;
import com.kayhut.fuse.model.execution.plan.Plan;
import com.kayhut.fuse.model.execution.plan.PlanOpBase;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;

/**
 * Created by Roman on 11/05/2017.
 */
public class M1PlanOpTranslationStrategy extends CompositePlanOpTranslationStrategy {
    private static class EntityOpStrategies extends CompositePlanOpTranslationStrategy {
        public EntityOpStrategies() {
            super(
                    new EntityOpTranslationStrategy(EntityTranslationOptions.none),
                    new EntitySelectionTranslationStrategy(EntityOp.class)
            );
        }
    }

    private static class EntityFilterOpStrategies extends CompositePlanOpTranslationStrategy {
        public EntityFilterOpStrategies() {
            super(
                    new EntityFilterOpTranslationStrategy(EntityTranslationOptions.none)
            );
        }
    }

    //region Constructors
    public M1PlanOpTranslationStrategy() {
        super(new EntityOpStrategies(),
                new GoToEntityOpTranslationStrategy(),
                new RelationOpTranslationStrategy(),
                new EntityFilterOpStrategies(),
                new RelationFilterOpTranslationStrategy());
    }
    //endregion


    @Override
    public GraphTraversal translate(GraphTraversal traversal, Plan plan, PlanOpBase planOp, TranslationContext context) {
        return super.translate(traversal, plan, planOp, context);
    }
}
