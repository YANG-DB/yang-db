package com.kayhut.fuse.gta.strategy;

import com.kayhut.fuse.model.execution.plan.PlanOpBase;
import javaslang.collection.Stream;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;

import java.util.ArrayList;

/**
 * Created by Roman on 10/05/2017.
 */
public class CompositeTranslationStrategy implements TranslationStrategy {
    //region Constructors
    public CompositeTranslationStrategy(TranslationStrategy...strategies) {
        this.strategies = Stream.of(strategies).toJavaList();
    }
    //endregion

    //region TranslationStrategy Implementation
    @Override
    public GraphTraversal translate(GraphTraversal traversal, PlanOpBase planOp, TranslationStrategyContext context) {
        for(TranslationStrategy translationStrategy : this.strategies) {
            traversal = translationStrategy.translate(traversal, planOp, context);
        }

        return traversal;
    }
    //endregion

    //region Fields
    private Iterable<TranslationStrategy> strategies;
    //endregion
}
