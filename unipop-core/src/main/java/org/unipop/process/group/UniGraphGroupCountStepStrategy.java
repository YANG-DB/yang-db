//package org.unipop.process.group;

/*-
 * #%L
 * UniGraphGroupCountStepStrategy.java - unipop-core - kayhut - 2,016
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
//
//import org.apache.tinkerpop.gremlin.process.traversal.Scope;
//import org.apache.tinkerpop.gremlin.process.traversal.Traversal;
//import org.apache.tinkerpop.gremlin.process.traversal.TraversalStrategy;
//import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__;
//import org.apache.tinkerpop.gremlin.process.traversal.step.map.GroupCountStep;
//import org.apache.tinkerpop.gremlin.process.traversal.step.map.GroupStep;
//import org.apache.tinkerpop.gremlin.process.traversal.strategy.AbstractTraversalStrategy;
//import org.apache.tinkerpop.gremlin.process.traversal.util.TraversalHelper;
//import org.apache.tinkerpop.gremlin.structure.Graph;
//import org.unipop.structure.UniGraph;
//
///**
// * Created by Roman on 11/12/2015.
// */
//public class UniGraphGroupCountStepStrategy extends AbstractTraversalStrategy<TraversalStrategy.ProviderOptimizationStrategy> implements TraversalStrategy.ProviderOptimizationStrategy {
//    //region AbstractTraversalStrategy Implementation
//    @Override
//    public void apply(Traversal.Admin<?, ?> traversal) {
//        if (traversal.getEngine().isComputer()) return;
//
//        Graph graph = traversal.getGraph().get();
//        if (!(graph instanceof UniGraph)) return;
//
//        TraversalHelper.getStepsOfAssignableClassRecursively(GroupCountStep.class, traversal).forEach(groupCountStep -> {
//            Traversal keyTraversal = groupCountStep.getLocalChildren().size() > 0 ?
//                    (Traversal)groupCountStep.getLocalChildren().get(0) :
//                    __.map(traverser -> traverser.get());
//            Traversal valueTraversal = keyTraversal.asAdmin().clone();
//            Traversal reducerTraversal = __.count(Scope.local);
//
//            GroupStep groupStep = new GroupStep(traversal);
//            groupStep.addLocalChild(keyTraversal.asAdmin());
//            groupStep.addLocalChild(valueTraversal.asAdmin());
//            groupStep.addLocalChild(reducerTraversal.asAdmin());
//            groupCountStep.getLabels().forEach(label -> groupStep.addLabel(label.toString()));
//
//            TraversalHelper.replaceStep(groupCountStep, groupStep, traversal);
//        });
//    }
//}
