package com.kayhut.fuse.gta.strategy.discrete;

import com.kayhut.fuse.gta.strategy.common.CompositePlanOpTranslationStrategy;
import com.kayhut.fuse.gta.strategy.common.EntityTranslationOptions;
import com.kayhut.fuse.gta.strategy.common.GoToEntityOpTranslationStrategy;
import com.kayhut.fuse.dispatcher.gta.TranslationContext;
import com.kayhut.fuse.model.execution.plan.*;
import com.kayhut.fuse.model.execution.plan.composite.Plan;
import com.kayhut.fuse.model.execution.plan.costs.PlanDetailedCost;
import com.kayhut.fuse.model.execution.plan.entity.EntityFilterOp;
import com.kayhut.fuse.model.execution.plan.entity.EntityOp;
import com.kayhut.fuse.model.execution.plan.relation.RelationFilterOp;
import com.kayhut.fuse.model.execution.plan.relation.RelationOp;
import javaslang.collection.Stream;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;

/**
 * Created by Roman on 11/05/2017.
 */
public class M1PlanOpTranslationStrategy extends CompositePlanOpTranslationStrategy {
    //region Constructors
    public M1PlanOpTranslationStrategy() {
        super();

        this.strategies = Stream.of(
                new CompositePlanOpTranslationStrategy(
                        new EntityOpTranslationStrategy(EntityTranslationOptions.none),
                        new EntitySelectionTranslationStrategy(planOp -> planOp.getClass().equals(EntityOp.class))),

                new CompositePlanOpTranslationStrategy(
                        new EntityFilterOpTranslationStrategy(EntityTranslationOptions.none),
                        new EntitySelectionTranslationStrategy(planOp -> planOp.getClass().equals(EntityFilterOp.class))),

                new GoToEntityOpTranslationStrategy(),

                new CompositePlanOpTranslationStrategy(
                        new RelationOpTranslationStrategy(),
                        new RelationSelectionTranslationStrategy(planOp -> planOp.getClass().equals(RelationOp.class))),

                new CompositePlanOpTranslationStrategy(
                        new RelationFilterOpTranslationStrategy(),
                        new RelationSelectionTranslationStrategy(planOp -> planOp.getClass().equals(RelationFilterOp.class))),

                new OptionalOpTranslationStrategy(this)
        ).toJavaList();
    }
    //endregion


    @Override
    public GraphTraversal translate(GraphTraversal traversal, PlanWithCost<Plan, PlanDetailedCost> plan, PlanOp planOp, TranslationContext context) {
        return super.translate(traversal, plan, planOp, context);
    }
}
