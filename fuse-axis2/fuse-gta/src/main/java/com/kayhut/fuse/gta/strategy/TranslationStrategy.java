package com.kayhut.fuse.gta.strategy;

import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;

import java.util.function.BiFunction;

/**
 * Created by benishue on 08-Mar-17.
 */
public interface TranslationStrategy extends BiFunction<TranslationStrategyContext, GraphTraversal, GraphTraversal> {
    /**
     * traversal returns same instance as in parameter
     * @param context
     * @param traversal - is mutated parameter
     * @return
     */
    GraphTraversal apply(TranslationStrategyContext context, GraphTraversal traversal);
}
