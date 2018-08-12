package com.kayhut.fuse.unipop.process.edge;

import org.apache.tinkerpop.gremlin.process.traversal.Traversal;
import org.apache.tinkerpop.gremlin.process.traversal.TraversalStrategy;
import org.apache.tinkerpop.gremlin.process.traversal.step.map.EdgeOtherVertexStep;
import org.apache.tinkerpop.gremlin.process.traversal.step.map.EdgeVertexStep;
import org.apache.tinkerpop.gremlin.process.traversal.strategy.AbstractTraversalStrategy;
import org.apache.tinkerpop.gremlin.process.traversal.util.TraversalHelper;
import org.unipop.process.edge.UniGraphEdgeVertexStep;
import org.unipop.structure.UniGraph;

public class FuseEdgeStepsStrategy extends AbstractTraversalStrategy<TraversalStrategy.ProviderOptimizationStrategy> implements TraversalStrategy.ProviderOptimizationStrategy {
    @Override
    public void apply(Traversal.Admin<?, ?> traversal) {
        UniGraph uniGraph = ((UniGraph) traversal.getGraph().get());

        TraversalHelper.getStepsOfClass(EdgeOtherVertexStep.class, traversal).forEach(edgeOtherVertexStep -> {
            UniGraphEdgeOtherVertexStep uniGraphEdgeOtherVertexStep = new UniGraphEdgeOtherVertexStep(traversal);
            edgeOtherVertexStep.getLabels().forEach(uniGraphEdgeOtherVertexStep::addLabel);
            TraversalHelper.replaceStep(edgeOtherVertexStep, uniGraphEdgeOtherVertexStep, traversal);
        });

        TraversalHelper.getStepsOfClass(EdgeVertexStep.class, traversal).forEach(edgeVertexStep -> {
            UniGraphEdgeVertexStep uniGraphEdgeVertexStep = new UniGraphEdgeVertexStep(traversal, edgeVertexStep.getDirection(), uniGraph, uniGraph.getControllerManager());
            edgeVertexStep.getLabels().forEach(uniGraphEdgeVertexStep::addLabel);
            TraversalHelper.replaceStep(edgeVertexStep, uniGraphEdgeVertexStep, traversal);
        });
    }
}
