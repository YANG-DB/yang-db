package org.unipop.process.start;

import javaslang.collection.Stream;
import org.apache.tinkerpop.gremlin.process.traversal.Step;
import org.apache.tinkerpop.gremlin.process.traversal.Traversal;
import org.apache.tinkerpop.gremlin.process.traversal.TraversalStrategy;
import org.apache.tinkerpop.gremlin.process.traversal.step.map.CountGlobalStep;
import org.apache.tinkerpop.gremlin.process.traversal.strategy.AbstractTraversalStrategy;
import org.apache.tinkerpop.gremlin.process.traversal.util.TraversalHelper;
import org.apache.tinkerpop.gremlin.structure.Edge;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.unipop.process.vertex.UniGraphVertexStep;
import org.unipop.process.vertex.UniGraphVertexStepStrategy;
import org.unipop.structure.UniGraph;

import java.util.Collections;
import java.util.Set;



/**
 * Created by Roman on 3/14/2018.
 */
public class UniGraphStartEdgeCountStepStrategy extends AbstractTraversalStrategy<TraversalStrategy.ProviderOptimizationStrategy> implements TraversalStrategy.ProviderOptimizationStrategy{
    //region AbstractTraversalStrategy Implementation
    @Override
    public void apply(Traversal.Admin<?, ?> traversal) {
        if(TraversalHelper.onGraphComputer(traversal)) return;

        Graph graph = traversal.getGraph().get();
        if(!(graph instanceof UniGraph)) {
            return;
        }

        UniGraph uniGraph = (UniGraph) graph;

        TraversalHelper.getStepsOfClass(UniGraphStartStep.class, traversal).forEach(uniGraphStartStep -> {
            Step nextStep = uniGraphStartStep.getNextStep();
            if (nextStep instanceof UniGraphVertexStep && ((UniGraphVertexStep) nextStep).getReturnClass().isAssignableFrom(Edge.class)&& nextStep.getNextStep() instanceof CountGlobalStep) {
                uniGraphStartStep.getTraversal().removeStep(nextStep);
                uniGraphStartStep.getTraversal().removeStep(nextStep.getNextStep());

                final UniGraphStartEdgeCountStep<?> uniGraphStartCountStep = new UniGraphStartEdgeCountStep<>(uniGraphStartStep, (UniGraphVertexStep) nextStep, uniGraph.getControllerManager());
                TraversalHelper.replaceStep(uniGraphStartStep, uniGraphStartCountStep, traversal);
            }
        });
    }
    //endregion
}
