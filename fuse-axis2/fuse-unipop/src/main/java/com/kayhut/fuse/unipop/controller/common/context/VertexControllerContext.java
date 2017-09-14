package com.kayhut.fuse.unipop.controller.common.context;

import com.kayhut.fuse.unipop.promise.TraversalConstraint;
import com.kayhut.fuse.unipop.schemaProviders.GraphElementSchemaProvider;
import com.kayhut.fuse.unipop.structure.ElementType;
import javaslang.Tuple2;
import javaslang.collection.Stream;
import org.apache.tinkerpop.gremlin.structure.Direction;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.unipop.structure.UniGraph;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Created by roman.margolis on 13/09/2017.
 */
public interface VertexControllerContext extends BulkContext, DirectionContext, ElementControllerContext {
    class Default extends ElementControllerContext.Default implements VertexControllerContext {
        //region Constructors
        public Default(
                UniGraph graph,
                GraphElementSchemaProvider schemaProvider,
                Optional<TraversalConstraint> constraint,
                Direction direction,
                Iterable<Vertex> bulkVertices) {
            super(graph, ElementType.edge, schemaProvider, constraint);
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
