package org.unipop.process.union;

import javaslang.collection.Stream;
import org.apache.tinkerpop.gremlin.process.traversal.Traversal;
import org.apache.tinkerpop.gremlin.process.traversal.TraversalStrategy;
import org.apache.tinkerpop.gremlin.process.traversal.step.TraversalParent;
import org.apache.tinkerpop.gremlin.process.traversal.step.branch.BranchStep;
import org.apache.tinkerpop.gremlin.process.traversal.step.branch.UnionStep;
import org.apache.tinkerpop.gremlin.process.traversal.step.sideEffect.IdentityStep;
import org.apache.tinkerpop.gremlin.process.traversal.step.util.ComputerAwareStep;
import org.apache.tinkerpop.gremlin.process.traversal.strategy.AbstractTraversalStrategy;
import org.apache.tinkerpop.gremlin.process.traversal.util.TraversalHelper;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.unipop.structure.UniGraph;

import java.util.Collections;
import java.util.Set;

/**
 * Created by roman.margolis on 28/11/2017.
 */
public class UniGraphUnionStepNewStrategy extends AbstractTraversalStrategy<TraversalStrategy.ProviderOptimizationStrategy> implements TraversalStrategy.ProviderOptimizationStrategy {
    //region AbstractTraversalStrategy Implementation
    @Override
    public void apply(Traversal.Admin<?, ?> traversal) {
        if(TraversalHelper.onGraphComputer(traversal)) {
            return;
        }

        Graph graph = traversal.getGraph().get();
        if(!(graph instanceof UniGraph)) {
            return;
        }

        UniGraph uniGraph = (UniGraph) graph;

        Stream.ofAll(TraversalHelper.getStepsOfAssignableClassRecursively(UnionStep.class, traversal))
                .forEach(branchStep -> {
                    UniGraphUnionNewStep graphUnionStep = new UniGraphUnionNewStep(traversal, uniGraph, branchStep.getGlobalChildren());
                    Traversal.Admin<?, ?> branchTraversal = (Traversal.Admin)branchStep.getLocalChildren().get(0);
                    TraversalHelper.replaceStep(branchStep, graphUnionStep, branchStep.getTraversal());
                    branchTraversal.removeStep(branchTraversal.getEndStep());
                    graphUnionStep.addGlobalChild(branchTraversal);

                    if (TraversalHelper.stepIndex(branchStep, traversal) != -1) {
                        TraversalHelper.replaceStep(branchStep, graphUnionStep, traversal);
                    } else {
                        TraversalHelper.getStepsOfAssignableClass(TraversalParent.class, traversal).forEach(traversalParent -> {
                            traversalParent.getLocalChildren().forEach(child -> {
                                if(TraversalHelper.stepIndex(branchStep, child) != -1) {
                                    TraversalHelper.replaceStep(branchStep, graphUnionStep, child);
                                }
                            });
                            traversalParent.getGlobalChildren().forEach(child -> {
                                if(TraversalHelper.stepIndex(branchStep, child) != -1) {
                                    TraversalHelper.replaceStep(branchStep, graphUnionStep, child);
                                }
                            });
                        });
                    }
                });
    }

    //endregion
}
