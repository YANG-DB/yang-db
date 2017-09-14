package com.kayhut.fuse.unipop.controller.promise.context;

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
 * Created by Elad on 4/26/2017.
 */
public class PromiseVertexControllerContext extends VertexControllerContext.Default {
    public PromiseVertexControllerContext(UniGraph graph, GraphElementSchemaProvider schemaProvider, Optional<TraversalConstraint> constraint, Iterable<Vertex> bulkVertices) {
        super(graph, schemaProvider, constraint, Direction.OUT, bulkVertices);
    }
}
