package com.kayhut.fuse.unipop.controller.discrete.context;

import com.kayhut.fuse.unipop.controller.common.context.ConstraintContext;
import com.kayhut.fuse.unipop.controller.common.context.ElementControllerContext;
import com.kayhut.fuse.unipop.promise.TraversalConstraint;
import com.kayhut.fuse.unipop.schemaProviders.GraphElementSchemaProvider;
import com.kayhut.fuse.unipop.structure.ElementType;

import java.util.Optional;

/**
 * Created by roman.margolis on 12/09/2017.
 */
public class DiscreteElementControllerContext implements ElementControllerContext {
    //region Constructors
    public DiscreteElementControllerContext(
            Optional<TraversalConstraint> constraint,
            ElementType elementType,
            GraphElementSchemaProvider schemaProvider) {
        this.constraint = constraint;
        this.elementType = elementType;
        this.schemaProvider = schemaProvider;
    }
    //endregion

    //region Properties
    @Override
    public Optional<TraversalConstraint> getConstraint() {
        return this.constraint;
    }

    @Override
    public ElementType getElementType() {
        return this.elementType;
    }

    @Override
    public GraphElementSchemaProvider getSchemaProvider() {
        return this.schemaProvider;
    }
    //endregion

    //region Fields
    private Optional<TraversalConstraint> constraint;
    private ElementType elementType;
    private GraphElementSchemaProvider schemaProvider;
    //endregion
}
