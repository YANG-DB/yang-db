package org.unipop.process.union;

import org.apache.tinkerpop.gremlin.process.traversal.Step;
import org.apache.tinkerpop.gremlin.process.traversal.Traversal;
import org.apache.tinkerpop.gremlin.process.traversal.TraversalStrategy;
import org.apache.tinkerpop.gremlin.process.traversal.step.branch.UnionStep;
import org.apache.tinkerpop.gremlin.process.traversal.step.util.EmptyStep;
import org.apache.tinkerpop.gremlin.process.traversal.strategy.AbstractTraversalStrategy;
import org.apache.tinkerpop.gremlin.process.traversal.util.TraversalHelper;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.unipop.structure.UniGraph;

import static javaslang.collection.Stream.ofAll;

public class UniGraphUnionStepNewStrategy extends AbstractTraversalStrategy<TraversalStrategy.ProviderOptimizationStrategy> implements TraversalStrategy.ProviderOptimizationStrategy {
    //region AbstractTraversalStrategy Implementation
    @Override
    public void apply(Traversal.Admin<?, ?> traversal) {
        if (TraversalHelper.onGraphComputer(traversal)) {
            return;
        }

        Graph graph = traversal.getGraph().get();
        if (!(graph instanceof UniGraph)) {
            return;
        }

        UniGraph uniGraph = (UniGraph) graph;

        final Step<?, ?> startStep = traversal.getStartStep();
        if (UnionStep.class.isAssignableFrom(startStep.getClass()) && traversal.getParent().equals(EmptyStep.instance())) {
            final UnionStep<?, ?> unionOriginStep = (UnionStep<?, ?>) startStep;
            UniGraphUnionNewStep unionNewStep = new UniGraphUnionNewStep(traversal, unionOriginStep.getGlobalChildren());
            TraversalHelper.replaceStep(unionOriginStep, unionNewStep, traversal);
        }
        ofAll(TraversalHelper.getStepsOfAssignableClassRecursively(UnionStep.class, traversal))
            .forEach(
                step -> {
                    UniGraphUnionBulkNewStep unionNewStep = new UniGraphUnionBulkNewStep(traversal, uniGraph, step.getGlobalChildren());
                    TraversalHelper.replaceStep(step, unionNewStep, traversal);
                });
    }

    //endregion
}
