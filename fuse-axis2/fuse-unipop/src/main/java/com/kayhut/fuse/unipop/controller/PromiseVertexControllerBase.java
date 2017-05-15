package com.kayhut.fuse.unipop.controller;

import com.kayhut.fuse.unipop.controller.utils.CollectionUtil;
import javaslang.collection.Stream;
import org.apache.tinkerpop.gremlin.process.traversal.step.util.HasContainer;
import org.apache.tinkerpop.gremlin.structure.Edge;
import org.apache.tinkerpop.gremlin.structure.T;
import org.unipop.query.search.SearchVertexQuery;

import java.util.*;

/**
 * Created by Roman on 15/05/2017.
 */
public abstract class PromiseVertexControllerBase implements SearchVertexQuery.SearchVertexController {
    //region Constructors
    PromiseVertexControllerBase(Iterable<String> supportedEdgeLabels) {
        this.supportedEdgeLabels = Stream.ofAll(supportedEdgeLabels).toJavaSet();
    }
    //endregion

    //region SearchVertexQuery.SearchVertexController Implementation
    @Override
    public Iterator<Edge> search(SearchVertexQuery searchVertexQuery) {
        Iterable<String> supportedEdgeLabels = getSupportedEdgeLabels(searchVertexQuery.getPredicates().getPredicates());
        return search(searchVertexQuery, supportedEdgeLabels);
    }
    //endregion

    //region Protected Methods
    protected Iterable<String> getSupportedEdgeLabels(Iterable<HasContainer> hasContainers) {
        Optional<HasContainer> labelHasContainer =
                Stream.ofAll(hasContainers)
                    .filter(hasContainer -> hasContainer.getKey().equals(T.label.getAccessor()))
                    .toJavaOptional();

        if (!labelHasContainer.isPresent()) {
            return Collections.emptyList();
        }

        List<String> requestedEdgeLabels = CollectionUtil.listFromObjectValue(labelHasContainer.get().getValue());
        return Stream.ofAll(requestedEdgeLabels).filter(label -> this.supportedEdgeLabels.contains(label)).toJavaList();
    }

    protected abstract Iterator<Edge> search(SearchVertexQuery searchVertexQuery, Iterable<String> edgeLabels);
    //endregion

    //region Fields
    private Set<String> supportedEdgeLabels;
    //endregion
}
