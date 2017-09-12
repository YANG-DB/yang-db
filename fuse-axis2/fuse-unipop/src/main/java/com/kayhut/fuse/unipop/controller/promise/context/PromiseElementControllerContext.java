package com.kayhut.fuse.unipop.controller.promise.context;

import com.kayhut.fuse.unipop.controller.common.context.ConstraintContext;
import com.kayhut.fuse.unipop.promise.*;
import com.kayhut.fuse.unipop.schemaProviders.*;
import com.kayhut.fuse.unipop.structure.ElementType;
import javaslang.collection.Stream;
import org.apache.tinkerpop.gremlin.process.traversal.step.util.HasContainer;
import org.unipop.query.search.SearchQuery;

import java.util.ArrayList;
import java.util.Optional;

/**
 * Created by User on 27/03/2017.
 */
public class PromiseElementControllerContext implements SizeAppenderContext, SelectContext, ConstraintContext{

    //region Constructors
    public PromiseElementControllerContext(
            Iterable<Promise> promises,
            Optional<TraversalConstraint> constraint,
            Iterable<HasContainer> selectPHasContainers,
            GraphElementSchemaProvider schemaProvider,
            ElementType elementType,
            SearchQuery searchQuery) {
        this.promises = new ArrayList<>(Stream.ofAll(promises).toJavaList());
        this.constraint = constraint;
        this.selectPHasContainers = selectPHasContainers;
        this.schemaProvider = schemaProvider;
        this.elementType = elementType;
        this.searchQuery = searchQuery;
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
    private Optional<TraversalConstraint> constraint;
    private GraphElementSchemaProvider schemaProvider;
    private ElementType elementType;
    private SearchQuery searchQuery;
    private Iterable<HasContainer> selectPHasContainers;
    //endregion

}
