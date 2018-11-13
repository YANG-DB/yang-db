package org.unipop.process.coalesce;

/*-
 * #%L
 * UniGraphCoalesceStepStrategy.java - unipop-core - kayhut - 2,016
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
import org.apache.tinkerpop.gremlin.process.traversal.step.map.CoalesceStep;
import org.apache.tinkerpop.gremlin.process.traversal.strategy.AbstractTraversalStrategy;
import org.apache.tinkerpop.gremlin.process.traversal.util.TraversalHelper;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.unipop.process.coalesce.UniGraphCoalesceStep;
import org.unipop.structure.UniGraph;

/**
 * Created by sbarzilay on 3/15/16.
 */
public class UniGraphCoalesceStepStrategy extends AbstractTraversalStrategy<TraversalStrategy.ProviderOptimizationStrategy> implements TraversalStrategy.ProviderOptimizationStrategy {
    @Override
    public void apply(Traversal.Admin<?, ?> traversal) {
        Graph graph = traversal.getGraph().get();
        if (!(graph instanceof UniGraph)) {
            return;
        }

        UniGraph uniGraph = (UniGraph) graph;

        TraversalHelper.getStepsOfClass(CoalesceStep.class, traversal).forEach(coalesceStep -> {
            UniGraphCoalesceStep uniGraphCoalesceStep = new UniGraphCoalesceStep(coalesceStep.getTraversal(), uniGraph, coalesceStep.getLocalChildren());
            TraversalHelper.replaceStep(coalesceStep, uniGraphCoalesceStep, traversal);
        });
    }
}
