package com.yangdb.fuse.unipop.process.traversal.strategy;

/*-
 * #%L
 * fuse-dv-unipop
 * %%
 * Copyright (C) 2016 - 2018 yangdb   ------ www.yangdb.org ------
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import com.yangdb.fuse.unipop.process.edge.FuseEdgeStepsStrategy;
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
import org.unipop.process.union.UniGraphUnionStepNewStrategy;
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
                        new UniGraphUnionStepNewStrategy(),
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
