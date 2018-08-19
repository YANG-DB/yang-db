package com.kayhut.fuse.unipop.process.traversal.strategy;

import com.kayhut.fuse.unipop.process.edge.FuseEdgeStepsStrategy;
import javaslang.collection.Stream;
import org.apache.tinkerpop.gremlin.process.traversal.TraversalStrategies;
import org.apache.tinkerpop.gremlin.process.traversal.TraversalStrategy;
import org.apache.tinkerpop.gremlin.process.traversal.strategy.optimization.PathRetractionStrategy;
import org.apache.tinkerpop.gremlin.process.traversal.util.DefaultTraversalStrategies;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.unipop.process.coalesce.UniGraphCoalesceStepStrategy;
import org.unipop.process.optional.UniGraphOptionalStepStrategy;
import org.unipop.process.order.UniGraphOrderStrategy;
import org.unipop.process.properties.UniGraphPropertiesStrategy;
import org.unipop.process.repeat.UniGraphRepeatStepStrategy;
import org.unipop.process.start.UniGraphStartCountStepStrategy;
import org.unipop.process.start.UniGraphStartEdgeCountStepStrategy;
import org.unipop.process.start.UniGraphStartStepStrategy;
import org.unipop.process.strategy.CompositeStrategy;
import org.unipop.process.strategyregistrar.StrategyProvider;
import org.unipop.process.union.UniGraphUnionStepStrategy;
import org.unipop.process.vertex.UniGraphVertexStepStrategy;
import org.unipop.process.where.UniGraphWhereStepStrategy;

import java.util.List;

public class FuseStandardStrategyProvider implements StrategyProvider {
    //region StrategyProvider Implementation
    @Override
    public TraversalStrategies get() {
        DefaultTraversalStrategies traversalStrategies = new DefaultTraversalStrategies();
        traversalStrategies.addStrategies(
                new CompositeStrategy(Stream.of(
                        new UniGraphStartStepStrategy(),
                        new UniGraphStartCountStepStrategy(),
                        new UniGraphVertexStepStrategy(),
                        new UniGraphStartEdgeCountStepStrategy(),
                        new FuseEdgeStepsStrategy(),
                        new UniGraphPropertiesStrategy(),
                        new UniGraphCoalesceStepStrategy(),
                        new UniGraphWhereStepStrategy(),
                        new UniGraphUnionStepStrategy(),
                        new UniGraphRepeatStepStrategy(),
                        new UniGraphOrderStrategy(),
                        new UniGraphOptionalStepStrategy()).toJavaList()
                ));

        if (globalTraversalStrategies == null) {
            globalTraversalStrategies = TraversalStrategies.GlobalCache.getStrategies(Graph.class).toList();
            globalTraversalStrategies = Stream.ofAll(globalTraversalStrategies).filter(strategy -> !(strategy instanceof PathRetractionStrategy)).toJavaList();
        }

        globalTraversalStrategies.forEach(traversalStrategies::addStrategies);

        return traversalStrategies;
    }
    //endregion

    //region Fields
    private static List<TraversalStrategy<?>> globalTraversalStrategies;
    //endregion


}
