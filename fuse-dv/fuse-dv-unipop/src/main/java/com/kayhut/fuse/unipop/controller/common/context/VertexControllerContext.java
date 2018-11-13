package com.kayhut.fuse.unipop.controller.common.context;

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

import com.kayhut.fuse.unipop.promise.TraversalConstraint;
import com.kayhut.fuse.unipop.schemaProviders.GraphElementSchemaProvider;
import com.kayhut.fuse.unipop.structure.ElementType;
import javaslang.Tuple2;
import javaslang.collection.Stream;
import org.apache.tinkerpop.gremlin.process.traversal.step.util.HasContainer;
import org.apache.tinkerpop.gremlin.structure.Direction;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.unipop.structure.UniGraph;

import java.util.Map;
import java.util.Optional;

/**
 * Created by roman.margolis on 13/09/2017.
 */
public interface VertexControllerContext extends BulkContext, DirectionContext, ElementControllerContext {
    class Impl extends ElementControllerContext.Impl implements VertexControllerContext {
        //region Constructors
        public Impl(
                UniGraph graph,
                ElementType elementType,
                GraphElementSchemaProvider schemaProvider,
                Optional<TraversalConstraint> constraint,
                Iterable<HasContainer> selectPHasContainers,
                int limit,
                Direction direction,
                Iterable<Vertex> bulkVertices) {
            super(graph, elementType, schemaProvider, constraint, selectPHasContainers, limit);
            this.direction = direction;
            this.bulkVertices = Stream.ofAll(bulkVertices).toJavaMap(vertex -> new Tuple2<>(vertex.id(), vertex));
        }
        //endregion

        //region VertexControllerContext Implementation
        @Override
        public Direction getDirection() {
            return this.direction;
        }

        @Override
        public Iterable<Vertex> getBulkVertices() {
            return bulkVertices.values();
        }

        @Override
        public Vertex getVertex(Object id) {
            return this.bulkVertices.get(id);
        }
        //endregion

        //region Fields
        private Direction direction;
        private Map<Object, Vertex> bulkVertices;
        //endregion
    }
}
