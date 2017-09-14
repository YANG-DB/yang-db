package com.kayhut.fuse.unipop.controller.discrete.context;

import com.kayhut.fuse.unipop.controller.common.context.BulkContext;
import com.kayhut.fuse.unipop.controller.common.context.ConstraintContext;
import com.kayhut.fuse.unipop.controller.common.context.VertexControllerContext;
import com.kayhut.fuse.unipop.promise.TraversalConstraint;
import com.kayhut.fuse.unipop.schemaProviders.GraphElementSchemaProvider;
import com.kayhut.fuse.unipop.structure.ElementType;
import org.apache.tinkerpop.gremlin.structure.Direction;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.unipop.structure.UniGraph;

import java.util.List;
import java.util.Optional;

/**
 * Created by roman.margolis on 13/09/2017.
 */
public class DiscreteVertexControllerContext extends VertexControllerContext.Default {
    public DiscreteVertexControllerContext(UniGraph graph, GraphElementSchemaProvider schemaProvider, Optional<TraversalConstraint> constraint, Direction direction, List<Vertex> bulkVertices) {
        super(graph, schemaProvider, constraint, direction, bulkVertices);
    }
}
