package com.kayhut.fuse.unipop.controller.promise.context;

import com.kayhut.fuse.unipop.controller.common.context.VertexControllerContext;
import com.kayhut.fuse.unipop.promise.TraversalConstraint;
import com.kayhut.fuse.unipop.schemaProviders.GraphElementSchemaProvider;
import com.kayhut.fuse.unipop.structure.ElementType;
import org.apache.tinkerpop.gremlin.process.traversal.step.util.HasContainer;
import org.apache.tinkerpop.gremlin.structure.Direction;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.unipop.query.search.SearchQuery;

import java.util.List;
import java.util.Optional;

/**
 * Created by Elad on 4/30/2017.
 */
public class PromiseVertexFilterControllerContext implements VertexControllerContext, SizeAppenderContext, SelectContext{
    //region Constructors
    public PromiseVertexFilterControllerContext(List<Vertex> vertices,
                                                Optional<TraversalConstraint> constraint,
                                                List<HasContainer> selectPHasContainers,
                                                GraphElementSchemaProvider schemaProvider,
                                                SearchQuery query) {
        this.bulkVertices = vertices;
        this.constraint = constraint;
        this.selectPHasContainers = selectPHasContainers;
        this.schema = schemaProvider;
        this.searchQuery = query;
    }
    //endregion

    //region Properties
    @Override
    public List<Vertex> getBulkVertices() {
        return bulkVertices;
    }

    @Override
    public Optional<TraversalConstraint> getConstraint() {
        return constraint;
    }

    @Override
    public ElementType getElementType() {
        return ElementType.vertex;
    }

    @Override
    public GraphElementSchemaProvider getSchemaProvider() {
        return this.schema;
    }

    @Override
    public SearchQuery getSearchQuery() {
        return searchQuery;
    }

    public Iterable<HasContainer> getSelectPHasContainers() {
        return selectPHasContainers;
    }

    @Override
    public Direction getDirection() {
        return Direction.OUT;
    }
    //endregion

    //region Fields
    private List<Vertex> bulkVertices;
    private Optional<TraversalConstraint> constraint;
    private GraphElementSchemaProvider schema;
    private SearchQuery searchQuery;
    private List<HasContainer> selectPHasContainers;
    //endregion
}
