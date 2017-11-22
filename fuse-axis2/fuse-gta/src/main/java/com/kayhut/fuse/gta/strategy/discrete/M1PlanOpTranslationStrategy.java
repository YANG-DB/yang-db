package com.kayhut.fuse.gta.strategy.discrete;

import com.kayhut.fuse.gta.strategy.common.CompositePlanOpTranslationStrategy;
import com.kayhut.fuse.gta.strategy.common.EntityTranslationOptions;
import com.kayhut.fuse.gta.strategy.common.GoToEntityOpTranslationStrategy;
import com.kayhut.fuse.gta.translation.TranslationContext;
import com.kayhut.fuse.model.execution.plan.*;
import com.kayhut.fuse.model.execution.plan.costs.PlanDetailedCost;
import javaslang.collection.Stream;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;

/**
 * Created by Roman on 11/05/2017.
 */
public class M1PlanOpTranslationStrategy extends CompositePlanOpTranslationStrategy {
    //region Constructors
    public M1PlanOpTranslationStrategy() {
        super(new CompositePlanOpTranslationStrategy(
                        new EntityOpTranslationStrategy(EntityTranslationOptions.none),
                        new EntitySelectionTranslationStrategy(EntityOp.class)),

                new CompositePlanOpTranslationStrategy(
                        new EntityFilterOpTranslationStrategy(EntityTranslationOptions.none),
                        new EntitySelectionTranslationStrategy(EntityFilterOp.class)),

                new GoToEntityOpTranslationStrategy(),

                new CompositePlanOpTranslationStrategy(
                        new RelationOpTranslationStrategy(),
                        new RelationSelectionTranslationStrategy(RelationOp.class)),

                new CompositePlanOpTranslationStrategy(
                        new RelationFilterOpTranslationStrategy(),
                        new RelationSelectionTranslationStrategy(RelationFilterOp.class))
                );

        this.strategies = Stream.ofAll(this.strategies).append(
                new OptionalOpTranslationStrategy(this)
        ).toJavaList();
    }
    //endregion


    @Override
    public GraphTraversal translate(GraphTraversal traversal, PlanWithCost<Plan, PlanDetailedCost> plan, PlanOpBase planOp, TranslationContext context) {
        return super.translate(traversal, plan, planOp, context);
    }
}
