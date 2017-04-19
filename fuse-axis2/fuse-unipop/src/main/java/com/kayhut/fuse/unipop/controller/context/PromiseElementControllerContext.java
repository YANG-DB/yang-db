package com.kayhut.fuse.unipop.controller.context;

import com.kayhut.fuse.unipop.promise.*;
import com.kayhut.fuse.unipop.schemaProviders.*;
import com.kayhut.fuse.unipop.structure.ElementType;
import javaslang.collection.Stream;
import org.unipop.query.search.SearchQuery;

import java.util.ArrayList;
import java.util.Optional;

/**
 * Created by User on 27/03/2017.
 */
public class PromiseElementControllerContext {

    //region Constructors
    public PromiseElementControllerContext(
            Iterable<Promise> promises,
            Optional<TraversalConstraint> constraint,
            GraphElementSchemaProvider schemaProvider,
            ElementType elementType,
            SearchQuery searchQuery) {
        this.promises = new ArrayList<>(Stream.ofAll(promises).toJavaList());
        this.constraint = constraint;
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
    //endregion

    //region Fields
    private Iterable<Promise> promises;
    private Optional<TraversalConstraint> constraint;
    private GraphElementSchemaProvider schemaProvider;
    private ElementType elementType;
    private SearchQuery searchQuery;
    //endregion

}
