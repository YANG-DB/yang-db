package com.kayhut.fuse.unipop.controller.context;

import com.kayhut.fuse.unipop.promise.Constraint;
import com.kayhut.fuse.unipop.promise.Promise;
import com.kayhut.fuse.unipop.promise.TraversalConstraint;
import com.kayhut.fuse.unipop.schemaProviders.GraphElementSchema;
import com.kayhut.fuse.unipop.schemaProviders.GraphElementSchemaProvider;
import com.kayhut.fuse.unipop.structure.ElementType;
import javaslang.collection.Stream;

import java.util.ArrayList;
import java.util.Optional;

/**
 * Created by User on 27/03/2017.
 */
public class PromiseElementControllerContext {

    //region Constructors
    public PromiseElementControllerContext(Iterable<Promise> promises, Optional<TraversalConstraint> constraint, GraphElementSchemaProvider schemaProvider, ElementType elementType) {
        this.promises = new ArrayList<>(Stream.ofAll(promises).toJavaList());
        this.constraint = constraint;
        this.schemaProvider = schemaProvider;
        this.elementType = elementType;
    }
    //endregion

    //region Properties
    public Iterable<Promise> getPromises() {
        return promises;
    }

    public GraphElementSchemaProvider getSchemaProvider() {
        return schemaProvider;
    }

    public Optional<TraversalConstraint> getConstraint() {
        return constraint;
    }

    public ElementType getElementType() {
        return elementType;
    }
    //endregion

    //region Fields
    private Iterable<Promise> promises;
    private Optional<TraversalConstraint> constraint;
    private GraphElementSchemaProvider schemaProvider;
    private ElementType elementType;
    //endregion

}
