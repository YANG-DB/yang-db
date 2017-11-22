package com.kayhut.fuse.unipop.controller.discrete.context;

import com.kayhut.fuse.unipop.controller.common.context.VertexControllerContext;
import com.kayhut.fuse.unipop.promise.TraversalConstraint;
import com.kayhut.fuse.unipop.schemaProviders.GraphElementSchemaProvider;
import com.kayhut.fuse.unipop.structure.ElementType;
import org.apache.tinkerpop.gremlin.process.traversal.step.util.HasContainer;
import org.apache.tinkerpop.gremlin.structure.Direction;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.unipop.structure.UniGraph;

import java.util.Optional;

/**
 * Created by roman.margolis on 13/09/2017.
 */
public class DiscreteVertexControllerContext extends VertexControllerContext.Impl {
    public DiscreteVertexControllerContext(UniGraph graph, GraphElementSchemaProvider schemaProvider, Optional<TraversalConstraint> constraint, Iterable<HasContainer> selectPHasContainers, int limit, Direction direction, Iterable<Vertex> bulkVertices) {
        super(graph, ElementType.edge, schemaProvider, constraint, selectPHasContainers, limit, direction, bulkVertices);
    }
}
