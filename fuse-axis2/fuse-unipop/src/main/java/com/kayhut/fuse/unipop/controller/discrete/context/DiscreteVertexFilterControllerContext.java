package com.kayhut.fuse.unipop.controller.discrete.context;

import com.kayhut.fuse.unipop.controller.common.context.VertexControllerContext;
import com.kayhut.fuse.unipop.promise.TraversalConstraint;
import com.kayhut.fuse.unipop.schemaProviders.GraphElementSchemaProvider;
import com.kayhut.fuse.unipop.structure.ElementType;
import org.apache.tinkerpop.gremlin.process.traversal.step.util.HasContainer;
import org.apache.tinkerpop.gremlin.structure.Direction;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.unipop.structure.UniGraph;

import java.util.List;
import java.util.Optional;

/**
 * Created by roman.margolis on 15/11/2017.
 */
public class DiscreteVertexFilterControllerContext  extends VertexControllerContext.Impl {
    //region Constructors
    public DiscreteVertexFilterControllerContext(
            UniGraph graph,
            List<Vertex> vertices,
            Optional<TraversalConstraint> constraint,
            List<HasContainer> selectPHasContainers,
            GraphElementSchemaProvider schemaProvider,
            int limit) {
        super(graph, ElementType.vertex, schemaProvider, constraint, selectPHasContainers, limit, Direction.OUT, vertices);
    }
    //endregion
}
