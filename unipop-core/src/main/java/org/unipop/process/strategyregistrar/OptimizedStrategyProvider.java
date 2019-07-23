//package org.unipop.process.strategyregistrar;

/*-
 * #%L
 * OptimizedStrategyProvider.java - unipop-core - yangdb - 2,016
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
//import org.apache.tinkerpop.gremlin.process.traversal.TraversalStrategies;
//import org.apache.tinkerpop.gremlin.process.traversal.util.DefaultTraversalStrategies;
//import org.apache.tinkerpop.gremlin.structure.Graph;
//import org.unipop.process.predicate.PredicatesUtil;
//import org.unipop.process.start.UniGraphStartStepStrategy;
//import org.unipop.process.reduce.UniGraphCountStepStrategy;
//import org.unipop.process.group.UniGraphGroupCountStepStrategy;
//import org.unipop.process.group.UniGraphGroupStepStrategy;
//import org.unipop.process.vertex.UniGraphVertexStepStrategy;
//import org.unipop.structure.UniGraph;
//
///**
// * Created by Gilad on 01/11/2015.
// */
//public class OptimizedStrategyRegistrar implements StrategyProvider {
//    //region org.unipop.process.strategyregistrar.StrategyProvider Implementation
//    @Override
//    public void get() {
//        try {
//            DefaultTraversalStrategies strategies = new DefaultTraversalStrategies();
//            strategies.addStrategies(
//                    //add strategies here
//                    new UniGraphStartStepStrategy(),
//                    new UniGraphVertexStepStrategy(),
//                    new PredicatesUtil(),
//                    new UniGraphGroupCountStepStrategy(),
//                    new UniGraphCountStepStrategy(),
//                    new UniGraphGroupStepStrategy()
//            );
//
//            TraversalStrategies.GlobalCache.getStrategies(Graph.class).toList().forEach(strategies::addStrategies);
//            TraversalStrategies.GlobalCache.registerStrategies(UniGraph.class, strategies);
//        } catch (Exception ex) {
//            //TODO: something productive
//        }
//    }
//    //endregion
//}
