package com.kayhut.fuse.unipop.controller;

import com.kayhut.fuse.unipop.controller.context.PromiseElementControllerContext;
import com.kayhut.fuse.unipop.controller.search.SearchBuilder;
import com.kayhut.fuse.unipop.controller.search.appender.CompositeSearchAppender;
import com.kayhut.fuse.unipop.controller.search.appender.ElementConstraintSearchAppender;
import com.kayhut.fuse.unipop.controller.search.appender.ElementGlobalTypeSearchAppender;
import com.kayhut.fuse.unipop.controller.search.appender.IndexSearchAppender;
import com.kayhut.fuse.unipop.controller.utils.CollectionUtils;
import com.kayhut.fuse.unipop.converter.CompositeConverter;
import com.kayhut.fuse.unipop.converter.SearchHitScrollIterable;
import com.kayhut.fuse.unipop.promise.Constraint;
import com.kayhut.fuse.unipop.promise.Promise;
import com.kayhut.fuse.unipop.promise.TraversalConstraint;
import com.kayhut.fuse.unipop.schemaProviders.GraphElementSchemaProvider;
import com.kayhut.fuse.unipop.structure.ElementType;
import com.kayhut.fuse.unipop.structure.PromiseVertex;
import javaslang.collection.Stream;
import org.apache.tinkerpop.gremlin.process.traversal.Compare;
import org.apache.tinkerpop.gremlin.process.traversal.Contains;
import org.apache.tinkerpop.gremlin.process.traversal.step.util.HasContainer;
import org.apache.tinkerpop.gremlin.structure.Element;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.client.Client;
import org.unipop.query.search.SearchQuery;
import org.unipop.structure.UniGraph;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.function.BiPredicate;

/**
 * Created by liorp on 4/2/2017.
 */
class VertexController implements SearchQuery.SearchController {

    //region Constructors
    VertexController(Client client, ElasticGraphConfiguration configuration, UniGraph graph, GraphElementSchemaProvider schemaProvider, CompositeConverter converter) {
        this.client = client;
        this.configuration = configuration;
        this.graph = graph;
        this.schemaProvider = schemaProvider;
        this.converter = converter;
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

    /**
     * This method will create promise vertices from promise and constraint predicates.
     * The method need not actually query the data store for the vertices as it is a promise vertex with a promise predicate.
     *
     * @param promiseHasContainers     - the promise predicate
     * @param constraintHasConstainers - the constraint predicate
     * @return the promise vertex iterator
     */
    private Iterator<Vertex> createPromiseVertices(List<HasContainer> promiseHasContainers, List<HasContainer> constraintHasConstainers) {
        List<Promise> promises = Collections.emptyList();
        BiPredicate promisePredicate = promiseHasContainers.get(0).getBiPredicate();

        if (promisePredicate.equals(Compare.eq)) {
            promises = Stream.of(promiseHasContainers.get(0).getValue()).map(p -> (Promise) p).toJavaList();
        } else if (promisePredicate.equals(Contains.within)) {
            promises = CollectionUtils.listFromObjectValue(promiseHasContainers.get(0).getValue());
        }

        Optional<Constraint> constraint = constraintHasConstainers.isEmpty() ?
                Optional.empty() :
                Optional.of((TraversalConstraint) constraintHasConstainers.get(0).getValue());

        return Stream.ofAll(promises).map(promise -> (Vertex) new PromiseVertex(promise, constraint, this.graph)).iterator();
    }

    /**
     * This method will query the data store and create promise vertices with the proper constraint predicate
     *
     *         //1. Build the SearchBuilder
     *         //1.1 Create the QueryAppender neccessary for the SearchBuilder
     *
     *         //1.2 Apply the QueryAppender with the propriate arguments and get the SearchBuilder
     *         //
     *         //2. Execute the SearchBuilder - run the query (elastic)
     *         //
     *         //3. Get the PromiseVertex Results
     *         //3.1   Get the elastic results
     *         //3.2   Build the ElementConverter
     *         //3.3   Apply the ElementConverter on the elastic results
     *         //3.4   return/build the PromiseVertex results iterator
     *
     * @param constraintHasContainers - the constraint predicate
     * @return the promise vertex iterator
     */
    private Iterator<Vertex> queryPromiseVertices(List<HasContainer> constraintHasContainers) {
        Optional<TraversalConstraint> constraint = constraintHasContainers.stream()
                .findFirst().filter(hasContainer -> hasContainer.getKey().toLowerCase().equals(GlobalConstants.HasKeys.CONSTRAINT))
                .map(h -> (TraversalConstraint) h.getValue());

        SearchBuilder builder = new SearchBuilder();
        PromiseElementControllerContext context = new PromiseElementControllerContext(Collections.EMPTY_LIST, constraint, schemaProvider, ElementType.vertex);
        //search appender
        CompositeSearchAppender<PromiseElementControllerContext> searchAppender = new CompositeSearchAppender<>(CompositeSearchAppender.Mode.all,
                new ElementConstraintSearchAppender(),
                new IndexSearchAppender(),
                new ElementGlobalTypeSearchAppender());

        searchAppender.append(builder, context);

        //compose
        SearchRequestBuilder compose = builder.compose(client, false);
        SearchHitScrollIterable searchHits = new SearchHitScrollIterable(configuration, compose, builder.getLimit(), client);
        return converter.convert(searchHits.iterator());
    }
    //endregion

    private Client client;
    private ElasticGraphConfiguration configuration;
    //region Fields
    private UniGraph graph;
    private GraphElementSchemaProvider schemaProvider;
    private CompositeConverter converter;
    //endregion
}
