package com.kayhut.fuse.unipop.controller.promise.context;

import com.kayhut.fuse.unipop.controller.common.context.ElementControllerContext;
import com.kayhut.fuse.unipop.promise.*;
import com.kayhut.fuse.unipop.schemaProviders.*;
import com.kayhut.fuse.unipop.structure.ElementType;
import javaslang.collection.Stream;
import org.apache.tinkerpop.gremlin.process.traversal.step.util.HasContainer;
import org.unipop.structure.UniGraph;

import java.util.ArrayList;
import java.util.Optional;

/**
 * Created by User on 27/03/2017.
 */
public class PromiseElementControllerContext extends ElementControllerContext.Impl {

    //region Constructors
    public PromiseElementControllerContext(
            UniGraph graph,
            Iterable<Promise> promises,
            Optional<TraversalConstraint> constraint,
            Iterable<HasContainer> selectPHasContainers,
            GraphElementSchemaProvider schemaProvider,
            ElementType elementType,
            int limit) {
        super(graph, elementType, schemaProvider, constraint, selectPHasContainers, limit);
        this.promises = new ArrayList<>(Stream.ofAll(promises).toJavaList());
    }
    //endregion

    //region Properties
    public Iterable<Promise> getPromises() {
        return promises;
    }
    //endregion

    //region Fields
    private Iterable<Promise> promises;
    //endregion

}
