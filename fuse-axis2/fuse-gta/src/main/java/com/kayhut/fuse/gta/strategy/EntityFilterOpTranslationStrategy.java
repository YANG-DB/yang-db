package com.kayhut.fuse.gta.strategy;

import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;

/**
 * Created by Roman on 09/05/2017.
 */
public class EntityFilterOpTranslationStrategy implements TranslationStrategy {
    //region TranslationStrategy Implementation
    @Override
    public GraphTraversal apply(TranslationStrategyContext context, GraphTraversal traversal) {
        return traversal;
    }
    //endregion
}
