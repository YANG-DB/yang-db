package com.kayhut.fuse.gta.strategy.common;

import com.kayhut.fuse.gta.strategy.PlanOpTranslationStrategy;
import com.kayhut.fuse.gta.translation.TranslationContext;
import com.kayhut.fuse.model.execution.plan.Plan;
import com.kayhut.fuse.model.execution.plan.PlanOpBase;
import com.kayhut.fuse.model.execution.plan.PlanWithCost;
import com.kayhut.fuse.model.execution.plan.costs.PlanDetailedCost;
import javaslang.collection.Stream;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;

/**
 * Created by Roman on 10/05/2017.
 */
public class CompositePlanOpTranslationStrategy implements PlanOpTranslationStrategy {
    //region Constructors
    public CompositePlanOpTranslationStrategy(PlanOpTranslationStrategy...strategies) {
        this.strategies = Stream.of(strategies).toJavaList();
    }

    public CompositePlanOpTranslationStrategy(Iterable<PlanOpTranslationStrategy> strategies) {
        this.strategies = Stream.ofAll(strategies).toJavaList();
    }
    //endregion

    //region PlanOpTranslationStrategy Implementation
    @Override
    public GraphTraversal translate(GraphTraversal traversal, PlanWithCost<Plan,PlanDetailedCost> plan, PlanOpBase planOp, TranslationContext context) {
        for(PlanOpTranslationStrategy planOpTranslationStrategy : this.strategies) {
            traversal = planOpTranslationStrategy.translate(traversal, plan, planOp, context);
        }

        return traversal;
    }
    //endregion

    //region Fields
    protected Iterable<PlanOpTranslationStrategy> strategies;
    //endregion
}
