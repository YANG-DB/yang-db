package org.unipop.process.optional;

/*-
 * #%L
 * UniGraphOptionalStepStrategy.java - unipop-core - kayhut - 2,016
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

import javaslang.collection.Stream;
import org.apache.tinkerpop.gremlin.process.traversal.Step;
import org.apache.tinkerpop.gremlin.process.traversal.Traversal;
import org.apache.tinkerpop.gremlin.process.traversal.TraversalStrategy;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__;
import org.apache.tinkerpop.gremlin.process.traversal.step.branch.BranchStep;
import org.apache.tinkerpop.gremlin.process.traversal.step.map.VertexStep;
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
public class UniGraphOptionalStepStrategy extends AbstractTraversalStrategy<TraversalStrategy.ProviderOptimizationStrategy> implements TraversalStrategy.ProviderOptimizationStrategy {
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

        Stream.ofAll(TraversalHelper.getStepsOfAssignableClassRecursively(BranchStep.class, traversal))
                .filter(this::isBranchEquivalentToOptional)
                .forEach(branchStep -> {
                    Traversal.Admin<?, ?> branchTraversal = (Traversal.Admin<?, ?>)branchStep.getLocalChildren().get(0);
                    branchTraversal.removeStep(branchTraversal.getEndStep());

                    UniGraphOptionalStep uniGraphOptionalStep = new UniGraphOptionalStep(traversal, uniGraph);
                    TraversalHelper.replaceStep(branchStep, uniGraphOptionalStep, branchStep.getTraversal());
                    uniGraphOptionalStep.addGlobalChild(branchTraversal);
                });
    }
    //endregion

    //region Private Methods
    private boolean isBranchEquivalentToOptional(BranchStep<?, ?, ?> branchStep) {
        if (branchStep.getGlobalChildren().size() != 2) {
            return false;
        }

        Traversal.Admin<?, ?> falseOption = branchStep.getGlobalChildren().get(0);
        Traversal.Admin<?, ?> trueOption = branchStep.getGlobalChildren().get(1);

        if (!isIdentityTraversal(falseOption)) {
            return false;
        }

        if (branchStep.getLocalChildren().size() != 1) {
            return false;
        }

        Traversal.Admin<?, ?> branchTraversal = branchStep.getLocalChildren().get(0).clone();
        branchTraversal.removeStep(branchTraversal.getEndStep()).addStep(trueOption.getEndStep().clone());

        if (!trueOption.equals(branchTraversal)) {
            return false;
        }

        return true;
    }

    private boolean isIdentityTraversal(Traversal.Admin<?, ?> traversal) {
        Set<Class> stepClasses = Stream.ofAll(traversal.getSteps()).map(step -> (Class)step.getClass()).toJavaSet();
        return stepClasses.equals(Collections.singleton(IdentityStep.class)) ||
                stepClasses.equals(Stream.of(IdentityStep.class, ComputerAwareStep.EndStep.class).toJavaSet());
    }
    //endregion
}
