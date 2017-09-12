package com.kayhut.fuse.unipop.controller.promise.context;

import com.kayhut.fuse.unipop.promise.TraversalConstraint;
import com.kayhut.fuse.unipop.schemaProviders.GraphElementSchemaProvider;
import org.apache.tinkerpop.gremlin.process.traversal.step.util.HasContainer;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.unipop.query.search.SearchQuery;

import java.util.List;
import java.util.Optional;

/**
 * Created by Elad on 4/30/2017.
 */
public class PromiseVertexFilterControllerContext implements EdgeConstraintContext, SizeAppenderContext, SelectContext{
    //region Constructors
    public PromiseVertexFilterControllerContext(List<Vertex> vertices,
                                                Optional<TraversalConstraint> constraint,
                                                List<HasContainer> selectPHasContainers,
                                                GraphElementSchemaProvider schemaProvider,
                                                SearchQuery query) {
        this.startVertices = vertices;
        this.edgeConstraint = constraint;
        this.selectPHasContainers = selectPHasContainers;
        this.schema = schemaProvider;
        this.searchQuery = query;
    }
    //endregion

    //region Properties
    public List<Vertex> getStartVertices() {
        return startVertices;
    }

    public Optional<TraversalConstraint> getEdgeConstraint() {
        return edgeConstraint;
    }

    public GraphElementSchemaProvider getSchema() {
        return schema;
    }

    @Override
    public SearchQuery getSearchQuery() {
        return searchQuery;
    }

    public Iterable<HasContainer> getSelectPHasContainers() {
        return selectPHasContainers;
    }

    //endregion

    //region Fields
    private List<Vertex> startVertices;
    private Optional<TraversalConstraint> edgeConstraint;
    private GraphElementSchemaProvider schema;
    private SearchQuery searchQuery;
    private List<HasContainer> selectPHasContainers;
    //endregion
}
