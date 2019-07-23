package com.yangdb.fuse.unipop.structure;

/*-
 * #%L
 * fuse-dv-unipop
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

import com.yangdb.fuse.unipop.process.traversal.dsl.graph.FuseGraphTraversalSource;
import org.apache.commons.configuration.Configuration;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.unipop.process.strategyregistrar.StrategyProvider;
import org.unipop.query.controller.ControllerManagerFactory;
import org.unipop.structure.UniGraph;

/**
 * Created by roman.margolis on 12/02/2018.
 */
public class FuseUniGraph extends UniGraph {
    //region Constructors
    public FuseUniGraph(Configuration configuration, ControllerManagerFactory controllerManagerFactory, StrategyProvider strategyProvider) throws Exception {
        super(configuration, controllerManagerFactory, strategyProvider);
    }
    //endregion

    //region Override Methods
    @Override
    public GraphTraversalSource traversal() {
        return new FuseGraphTraversalSource(this, this.strategies);
        //return new GraphTraversalSource(this, strategies);
    }
    //endregion
}
