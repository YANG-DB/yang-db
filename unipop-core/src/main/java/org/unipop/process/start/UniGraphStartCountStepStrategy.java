package org.unipop.process.start;

/*-
 * #%L
 * UniGraphStartCountStepStrategy.java - unipop-core - kayhut - 2,016
 * org.codehaus.mojo-license-maven-plugin-1.16
 * $Id$
 * $HeadURL$
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

import org.apache.tinkerpop.gremlin.process.traversal.Step;
import org.apache.tinkerpop.gremlin.process.traversal.Traversal;
import org.apache.tinkerpop.gremlin.process.traversal.TraversalStrategy;
import org.apache.tinkerpop.gremlin.process.traversal.step.map.CountGlobalStep;
import org.apache.tinkerpop.gremlin.process.traversal.step.map.GraphStep;
import org.apache.tinkerpop.gremlin.process.traversal.strategy.AbstractTraversalStrategy;
import org.apache.tinkerpop.gremlin.process.traversal.util.TraversalHelper;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.unipop.process.predicate.PredicatesUtil;
import org.unipop.structure.UniGraph;

/**
 * Created by Roman on 3/14/2018.
 */
public class UniGraphStartCountStepStrategy extends AbstractTraversalStrategy<TraversalStrategy.ProviderOptimizationStrategy> implements TraversalStrategy.ProviderOptimizationStrategy{
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
            if (uniGraphStartStep.getNextStep() instanceof CountGlobalStep) {
                uniGraphStartStep.getTraversal().removeStep(uniGraphStartStep.getNextStep());

                final UniGraphStartCountStep<?> uniGraphStartCountStep = new UniGraphStartCountStep<>(uniGraphStartStep, uniGraph.getControllerManager());
                TraversalHelper.replaceStep(uniGraphStartStep, uniGraphStartCountStep, traversal);
            }
        });
    }
    //endregion
}
