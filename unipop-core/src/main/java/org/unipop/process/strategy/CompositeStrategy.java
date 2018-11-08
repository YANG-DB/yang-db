package org.unipop.process.strategy;

import org.apache.tinkerpop.gremlin.process.traversal.Traversal;
import org.apache.tinkerpop.gremlin.process.traversal.TraversalStrategy;
import org.apache.tinkerpop.gremlin.process.traversal.TraversalStrategy.ProviderOptimizationStrategy;
import org.apache.tinkerpop.gremlin.process.traversal.strategy.AbstractTraversalStrategy;

import java.util.List;

public class CompositeStrategy extends AbstractTraversalStrategy<ProviderOptimizationStrategy> implements ProviderOptimizationStrategy {
    public CompositeStrategy(List<? extends AbstractTraversalStrategy<ProviderOptimizationStrategy>> innerStrategies) {
        this.innerStrategies = innerStrategies;
    }

    @Override
    public void apply(Traversal.Admin<?, ?> traversal) {
        innerStrategies.forEach(s -> s.apply(traversal));
    }

    private List<? extends AbstractTraversalStrategy<ProviderOptimizationStrategy>> innerStrategies;
}
