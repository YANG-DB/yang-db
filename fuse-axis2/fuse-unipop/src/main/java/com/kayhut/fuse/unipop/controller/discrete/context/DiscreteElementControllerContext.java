package com.kayhut.fuse.unipop.controller.discrete.context;

import com.kayhut.fuse.unipop.controller.common.context.ConstraintContext;
import com.kayhut.fuse.unipop.controller.common.context.ElementControllerContext;
import com.kayhut.fuse.unipop.promise.TraversalConstraint;
import com.kayhut.fuse.unipop.schemaProviders.GraphElementSchemaProvider;
import com.kayhut.fuse.unipop.structure.ElementType;
import org.unipop.structure.UniGraph;

import java.util.Optional;

/**
 * Created by roman.margolis on 12/09/2017.
 */
public class DiscreteElementControllerContext extends ElementControllerContext.Default {
    public DiscreteElementControllerContext(UniGraph graph, ElementType elementType, GraphElementSchemaProvider schemaProvider, Optional<TraversalConstraint> constraint) {
        super(graph, elementType, schemaProvider, constraint);
    }
}
