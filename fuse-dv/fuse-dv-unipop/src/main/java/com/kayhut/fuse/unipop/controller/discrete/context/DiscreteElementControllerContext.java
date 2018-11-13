package com.kayhut.fuse.unipop.controller.discrete.context;

/*-
 * #%L
 * fuse-dv-unipop
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

import com.kayhut.fuse.unipop.controller.common.context.ElementControllerContext;
import com.kayhut.fuse.unipop.promise.TraversalConstraint;
import com.kayhut.fuse.unipop.schemaProviders.GraphElementSchemaProvider;
import com.kayhut.fuse.unipop.structure.ElementType;
import org.apache.tinkerpop.gremlin.process.traversal.step.util.HasContainer;
import org.unipop.structure.UniGraph;

import java.util.Optional;

/**
 * Created by roman.margolis on 12/09/2017.
 */
public class DiscreteElementControllerContext extends ElementControllerContext.Impl {
    public DiscreteElementControllerContext(UniGraph graph, ElementType elementType, GraphElementSchemaProvider schemaProvider, Optional<TraversalConstraint> constraint, Iterable<HasContainer> selectPHasContainers, int limit) {
        super(graph, elementType, schemaProvider, constraint, selectPHasContainers, limit);
    }
}
