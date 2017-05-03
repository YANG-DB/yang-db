package com.kayhut.fuse.unipop.controller.context;

import com.kayhut.fuse.unipop.promise.TraversalConstraint;
import com.kayhut.fuse.unipop.schemaProviders.GraphElementSchemaProvider;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.unipop.query.search.SearchQuery;

import java.util.List;
import java.util.Optional;

/**
 * Created by Elad on 4/30/2017.
 */
public class PromiseVertexFilterControllerContext implements EdgeConstraintContext, SizeAppenderContext{

    private List<Vertex> startVertices;
    private Optional<TraversalConstraint> edgeConstraint;
    private GraphElementSchemaProvider schema;
    private SearchQuery searchQuery;

    public PromiseVertexFilterControllerContext(List<Vertex> vertices,
                                                Optional<TraversalConstraint> constraint,
                                                GraphElementSchemaProvider schemaProvider,
                                                SearchQuery query) {
        startVertices = vertices;
        edgeConstraint = constraint;
        schema = schemaProvider;
        searchQuery = query;
    }

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
}
