package com.kayhut.fuse.unipop.controller;

import com.kayhut.fuse.unipop.controller.utils.CollectionUtils;
import com.kayhut.fuse.unipop.promise.Constraint;
import com.kayhut.fuse.unipop.promise.Promise;
import com.kayhut.fuse.unipop.promise.TraversalConstraint;
import com.kayhut.fuse.unipop.structure.PromiseVertex;
import javaslang.collection.Stream;
import org.apache.tinkerpop.gremlin.process.traversal.Compare;
import org.apache.tinkerpop.gremlin.process.traversal.Contains;
import org.apache.tinkerpop.gremlin.process.traversal.step.util.HasContainer;
import org.apache.tinkerpop.gremlin.structure.Edge;
import org.apache.tinkerpop.gremlin.structure.Element;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.unipop.query.search.SearchQuery;
import org.unipop.structure.UniGraph;

import java.util.*;
import java.util.function.BiPredicate;

/**
 * Created by User on 19/03/2017.
 */
public class SearchPromiseElementController implements SearchQuery.SearchController {
    //region Constructors
    public SearchPromiseElementController(UniGraph graph) {
        this.innerControllers = new HashMap<>();
        this.innerControllers.put(Vertex.class, new VertexController(graph));
        this.innerControllers.put(Edge.class, new EdgeController());
    }
    //endregion

    //region SearchQuery.SearchController Implementation
    @Override
    public <E extends Element> Iterator<E> search(SearchQuery<E> searchQuery) {
        return this.innerControllers.get(searchQuery.getReturnType()).search(searchQuery);
    }
    //endregion

    //region Fields
    private Map<Class, SearchQuery.SearchController> innerControllers;
    private Graph graph;
    //endregion

    //region VertexController
    private class VertexController implements SearchQuery.SearchController {
        //region Constructors
        public VertexController(UniGraph graph) {
            this.graph = graph;
        }
        //endregion

        //region SearchQuery.SearchController Implementation
        @Override
        public <E extends Element> Iterator<E> search(SearchQuery<E> searchQuery) {
            List<HasContainer> promiseHasContainers = Stream.ofAll(searchQuery.getPredicates().getPredicates())
                    .filter(hasContainer -> hasContainer.getKey().toLowerCase().equals(GlobalConstants.HasKeys.PROMISE))
                    .toJavaList();
            if (promiseHasContainers.size() > 1) {
                throw new UnsupportedOperationException("Single \"" + GlobalConstants.HasKeys.PROMISE + "\" allowed");
            }

            List<HasContainer> constraintHasContainers = Stream.ofAll(searchQuery.getPredicates().getPredicates())
                    .filter(hasContainer -> hasContainer.getKey().toLowerCase().equals(GlobalConstants.HasKeys.CONSTRAINT))
                    .toJavaList();
            if (constraintHasContainers.size() > 1 ||
                    (!constraintHasContainers.isEmpty() && !constraintHasContainers.get(0).getBiPredicate().equals(Compare.eq))) {
                throw new UnsupportedOperationException("Single \"" + GlobalConstants.HasKeys.CONSTRAINT + "\" allowed");
            }

            return (Iterator<E>) (promiseHasContainers.isEmpty() ?
                                    queryPromiseVertices(constraintHasContainers) :
                                    createPromiseVertices(promiseHasContainers, constraintHasContainers));
        }
        //endregion

        //region Private Methods

        /** This method will create promise vertices from promise and constraint predicates.
         * The method need not actually query the data store for the vertices as it is a promise vertex with a promise predicate.
         * @param promiseHasContainers - the promise predicate
         * @param constraintHasConstainers - the constraint predicate
         * @return the promise vertex iterator
         */
        private Iterator<Vertex> createPromiseVertices(List<HasContainer> promiseHasContainers, List<HasContainer> constraintHasConstainers) {
            List<Promise> promises = Collections.emptyList();
            BiPredicate promisePredicate = promiseHasContainers.get(0).getBiPredicate();

            if (promisePredicate.equals(Compare.eq)) {
                promises = Stream.of(promiseHasContainers.get(0).getValue()).map(p -> (Promise)p).toJavaList();
            } else if (promisePredicate.equals(Contains.within)) {
                promises = CollectionUtils.listFromObjectValue(promiseHasContainers.get(0).getValue());
            }

            Optional<Constraint> constraint = constraintHasConstainers.isEmpty() ?
                    Optional.empty() :
                    Optional.of((TraversalConstraint) constraintHasConstainers.get(0).getValue());

            return Stream.ofAll(promises).map(promise -> (Vertex) new PromiseVertex(promise, constraint, this.graph)).iterator();
        }

        /** This method will query the data store and create promise vertices with the proper constraint predicate
         * @param constraintHasContainers - the constraint predicate
         * @return the promise vertex iterator
         */
        private Iterator<Vertex> queryPromiseVertices(List<HasContainer> constraintHasContainers) {
            return Collections.emptyIterator();
        }
        //endregion

        //region Fields
        private UniGraph graph;
        //endregion
    }
    //endregion

    //region EdgeController Implementation
    private class EdgeController implements SearchQuery.SearchController {
        //region SearchQuery.SearchController Implementation
        @Override
        public <E extends Element> Iterator<E> search(SearchQuery<E> searchQuery) {
            return null;
        }
        //endregion
    }
    //endregion
}
