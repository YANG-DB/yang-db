package com.kayhut.fuse.gta.strategy;

import com.kayhut.fuse.model.execution.plan.PlanOpBase;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;

import java.util.function.BiFunction;

/**
 * Created by benishue on 08-Mar-17.
 */
public interface TranslationStrategy {
    /**
     * traversal returns same instance as in parameter
     * @param context
     * @param traversal - is mutated parameter
     * @return
     */
    GraphTraversal translate(GraphTraversal traversal, PlanOpBase planOp, TranslationStrategyContext context);
}
