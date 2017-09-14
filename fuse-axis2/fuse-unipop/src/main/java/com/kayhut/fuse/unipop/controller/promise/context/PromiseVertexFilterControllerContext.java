package com.kayhut.fuse.unipop.controller.promise.context;

import com.kayhut.fuse.unipop.controller.common.context.VertexControllerContext;
import com.kayhut.fuse.unipop.promise.TraversalConstraint;
import com.kayhut.fuse.unipop.schemaProviders.GraphElementSchemaProvider;
import com.kayhut.fuse.unipop.structure.ElementType;
import org.apache.tinkerpop.gremlin.process.traversal.step.util.HasContainer;
import org.apache.tinkerpop.gremlin.structure.Direction;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.unipop.query.search.SearchQuery;
import org.unipop.structure.UniGraph;

import java.util.List;
import java.util.Optional;

/**
 * Created by Elad on 4/30/2017.
 */
public class PromiseVertexFilterControllerContext extends VertexControllerContext.Default implements SizeAppenderContext, SelectContext{
    //region Constructors
    public PromiseVertexFilterControllerContext(UniGraph graph,
                                                List<Vertex> vertices,
                                                Optional<TraversalConstraint> constraint,
                                                List<HasContainer> selectPHasContainers,
                                                GraphElementSchemaProvider schemaProvider,
                                                SearchQuery query) {
        super(graph, schemaProvider, constraint, Direction.OUT, vertices);
        this.searchQuery = query;
        this.selectPHasContainers = selectPHasContainers;
    }
    //endregion

    //region Properties
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
    private SearchQuery searchQuery;
    private List<HasContainer> selectPHasContainers;
    //endregion
}
