package org.unipop.process.vertex;

/*-
 * #%L
 * UniGraphVertexStepStrategy.java - unipop-core - kayhut - 2,016
 * org.codehaus.mojo-license-maven-plugin-1.16
 * $Id$
 * $HeadURL$
 * %%
 * Copyright (C) 2016 - 2018 kayhut
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

import org.apache.tinkerpop.gremlin.process.traversal.Traversal;
import org.apache.tinkerpop.gremlin.process.traversal.TraversalStrategy;
import org.apache.tinkerpop.gremlin.process.traversal.step.TraversalParent;
import org.apache.tinkerpop.gremlin.process.traversal.step.map.VertexStep;
import org.apache.tinkerpop.gremlin.process.traversal.strategy.AbstractTraversalStrategy;
import org.apache.tinkerpop.gremlin.process.traversal.util.TraversalHelper;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.unipop.process.predicate.PredicatesUtil;
import org.unipop.structure.UniGraph;


public class UniGraphVertexStepStrategy extends AbstractTraversalStrategy<TraversalStrategy.ProviderOptimizationStrategy> implements TraversalStrategy.ProviderOptimizationStrategy {

    @Override
    public void apply(Traversal.Admin<?, ?> traversal) {
        if(TraversalHelper.onGraphComputer(traversal)) return;

        Graph graph = traversal.getGraph().get();
        if(!(graph instanceof UniGraph)) {
            return;
        }

        UniGraph uniGraph = (UniGraph) graph;

        TraversalHelper.getStepsOfAssignableClassRecursively(VertexStep.class, traversal).forEach(vertexStep -> {
            if(TraversalHelper.stepIndex(vertexStep, traversal) != -1) {
                UniGraphVertexStep uniGraphVertexStep = new UniGraphVertexStep<>(vertexStep, uniGraph, uniGraph.getControllerManager());
                TraversalHelper.replaceStep(vertexStep, uniGraphVertexStep, traversal);
                if (vertexStep.returnsEdge()) PredicatesUtil.collectPredicates(uniGraphVertexStep, traversal);
            }
            else{
                TraversalHelper.getStepsOfAssignableClass(TraversalParent.class, traversal).forEach(traversalParent -> {
                    traversalParent.getLocalChildren().forEach(child -> {
                        if(TraversalHelper.stepIndex(vertexStep, child) != -1) {
                            UniGraphVertexStep uniGraphVertexStep = new UniGraphVertexStep<>(vertexStep, uniGraph, uniGraph.getControllerManager());
                            TraversalHelper.replaceStep(vertexStep, uniGraphVertexStep, child);
                            if (vertexStep.returnsEdge()) PredicatesUtil.collectPredicates(uniGraphVertexStep, child);
                        }
                    });
                    traversalParent.getGlobalChildren().forEach(child -> {
                        if(TraversalHelper.stepIndex(vertexStep, child) != -1) {
                            UniGraphVertexStep uniGraphVertexStep = new UniGraphVertexStep<>(vertexStep, uniGraph, uniGraph.getControllerManager());
                            TraversalHelper.replaceStep(vertexStep, uniGraphVertexStep, child);
                            if (vertexStep.returnsEdge()) PredicatesUtil.collectPredicates(uniGraphVertexStep, child);
                        }
                        else if (TraversalHelper.hasStepOfAssignableClass(TraversalParent.class, child)){
                            TraversalHelper.getStepsOfAssignableClass(TraversalParent.class, child).forEach(traversalParent1 -> {
                                traversalParent.getLocalChildren().forEach(child1 -> {
                                    if(TraversalHelper.stepIndex(vertexStep, child1) != -1) {
                                        UniGraphVertexStep uniGraphVertexStep = new UniGraphVertexStep<>(vertexStep, uniGraph, uniGraph.getControllerManager());
                                        TraversalHelper.replaceStep(vertexStep, uniGraphVertexStep, child1);
                                        if (vertexStep.returnsEdge()) PredicatesUtil.collectPredicates(uniGraphVertexStep, child1);
                                    }
                                });
                                traversalParent.getGlobalChildren().forEach(child1 -> {
                                    if(TraversalHelper.stepIndex(vertexStep, child1) != -1) {
                                        UniGraphVertexStep uniGraphVertexStep = new UniGraphVertexStep<>(vertexStep, uniGraph, uniGraph.getControllerManager());
                                        TraversalHelper.replaceStep(vertexStep, uniGraphVertexStep, child1);
                                        if (vertexStep.returnsEdge()) PredicatesUtil.collectPredicates(uniGraphVertexStep, child1);
                                    }
                                });
                            });
                        }
                    });
                });
            }
        });
    }
}
