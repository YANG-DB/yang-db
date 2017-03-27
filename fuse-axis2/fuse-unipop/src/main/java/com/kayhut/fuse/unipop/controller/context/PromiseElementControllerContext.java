package com.kayhut.fuse.unipop.controller.context;

import com.kayhut.fuse.unipop.promise.Constraint;
import com.kayhut.fuse.unipop.promise.Promise;
import com.kayhut.fuse.unipop.schemaProviders.GraphElementSchemaProvider;
import javaslang.collection.Stream;

import java.util.ArrayList;
import java.util.Optional;

/**
 * Created by User on 27/03/2017.
 */
public class PromiseElementControllerContext {
    //region Constructors
    public PromiseElementControllerContext(Iterable<Promise> promises, Optional<Constraint> constraint) {
        this.promises = new ArrayList<>(Stream.ofAll(promises).toJavaList());
        this.constraint = constraint;
    }
    //endregion

    //region Properties
    public Iterable<Promise> getPromises() {
        return promises;
    }

    public Optional<Constraint> getConstraint() {
        return constraint;
    }


    //endregion

    //region Fields
    private Iterable<Promise> promises;
    private Optional<Constraint> constraint;
    private GraphElementSchemaProvider schemaProvider;
    //endregion
}
