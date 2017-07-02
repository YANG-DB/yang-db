package com.kayhut.fuse.unipop.structure;

import com.google.common.collect.ImmutableMap;
import com.kayhut.fuse.unipop.controller.utils.map.MapBuilder;
import com.kayhut.fuse.unipop.promise.Constraint;
import com.kayhut.fuse.unipop.promise.Promise;
import org.apache.tinkerpop.gremlin.structure.T;
import org.unipop.structure.UniGraph;
import org.unipop.structure.UniVertex;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Created by User on 19/03/2017.
 */
public class PromiseVertex extends UniVertex {
    //region Constructor
    public PromiseVertex(Promise promise, Optional<Constraint> constraint, UniGraph graph) {
        this(promise, constraint, graph, Collections.emptyMap());
    }

    public PromiseVertex(Promise promise, Optional<Constraint> constraint, UniGraph graph, Map<String, Object> properties) {
        super(new MapBuilder<>(properties).put(T.id.getAccessor(), promise.getId()).get(), graph);

        this.promise = promise;
        this.constraint = constraint;
    }
    //endregion

    //region Override Methods
    @Override
    protected String getDefaultLabel() {
        return "promise";
    }
    //endregion

    //region properties
    public Promise getPromise() {
        return this.promise;
    }

    public Optional<Constraint> getConstraint() {

        return this.constraint;
    }
    //endregion

    //region Fields
    private Promise promise;
    private Optional<Constraint> constraint;
    //endregion
}
