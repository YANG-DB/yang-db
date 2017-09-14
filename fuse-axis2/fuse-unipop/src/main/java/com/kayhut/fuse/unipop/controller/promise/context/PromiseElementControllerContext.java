package com.kayhut.fuse.unipop.controller.promise.context;

import com.kayhut.fuse.unipop.controller.common.context.ConstraintContext;
import com.kayhut.fuse.unipop.controller.common.context.ElementControllerContext;
import com.kayhut.fuse.unipop.promise.*;
import com.kayhut.fuse.unipop.schemaProviders.*;
import com.kayhut.fuse.unipop.structure.ElementType;
import javaslang.collection.Stream;
import org.apache.tinkerpop.gremlin.process.traversal.step.util.HasContainer;
import org.unipop.query.search.SearchQuery;
import org.unipop.structure.UniGraph;

import java.util.ArrayList;
import java.util.Optional;

/**
 * Created by User on 27/03/2017.
 */
public class PromiseElementControllerContext extends ElementControllerContext.Default implements SizeAppenderContext, SelectContext{

    //region Constructors
    public PromiseElementControllerContext(
            UniGraph graph,
            Iterable<Promise> promises,
            Optional<TraversalConstraint> constraint,
            Iterable<HasContainer> selectPHasContainers,
            GraphElementSchemaProvider schemaProvider,
            ElementType elementType,
            SearchQuery searchQuery) {
        super(graph, elementType, schemaProvider, constraint);
        this.promises = new ArrayList<>(Stream.ofAll(promises).toJavaList());
        this.selectPHasContainers = selectPHasContainers;
        this.searchQuery = searchQuery;
    }
    //endregion

    //region Properties
    public Iterable<Promise> getPromises() {
        return promises;
    }

    public SearchQuery getSearchQuery() {
        return this.searchQuery;
    }

    @Override
    public Iterable<HasContainer> getSelectPHasContainers() {
        return this.selectPHasContainers;
    }
    //endregion

    //region Fields
    private Iterable<Promise> promises;
    private SearchQuery searchQuery;
    private Iterable<HasContainer> selectPHasContainers;
    //endregion

}
