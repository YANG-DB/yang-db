package com.kayhut.fuse.unipop.controller.promise;

import com.codahale.metrics.MetricRegistry;
import com.kayhut.fuse.unipop.controller.ElasticGraphConfiguration;
import com.kayhut.fuse.unipop.controller.common.appender.CompositeSearchAppender;
import com.kayhut.fuse.unipop.controller.common.appender.ElementGlobalTypeSearchAppender;
import com.kayhut.fuse.unipop.controller.common.appender.IndexSearchAppender;
import com.kayhut.fuse.unipop.controller.promise.context.PromiseElementControllerContext;
import com.kayhut.fuse.unipop.controller.search.SearchBuilder;
import com.kayhut.fuse.unipop.controller.promise.appender.*;
import com.kayhut.fuse.unipop.controller.utils.CollectionUtil;
import com.kayhut.fuse.unipop.controller.common.converter.ElementConverter;
import com.kayhut.fuse.unipop.controller.promise.converter.SearchHitPromiseVertexConverter;
import com.kayhut.fuse.unipop.converter.SearchHitScrollIterable;
import com.kayhut.fuse.unipop.predicates.SelectP;
import com.kayhut.fuse.unipop.promise.Constraint;
import com.kayhut.fuse.unipop.promise.Promise;
import com.kayhut.fuse.unipop.promise.TraversalConstraint;
import com.kayhut.fuse.unipop.schemaProviders.GraphElementSchemaProvider;
import com.kayhut.fuse.unipop.structure.ElementType;
import com.kayhut.fuse.unipop.structure.promise.PromiseVertex;
import javaslang.collection.Stream;
import org.apache.tinkerpop.gremlin.process.traversal.Compare;
import org.apache.tinkerpop.gremlin.process.traversal.Contains;
import org.apache.tinkerpop.gremlin.process.traversal.step.util.HasContainer;
import org.apache.tinkerpop.gremlin.structure.Element;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.client.Client;
import org.elasticsearch.search.SearchHit;
import org.unipop.query.search.SearchQuery;
import org.unipop.structure.UniGraph;

import java.util.*;
import java.util.function.BiPredicate;

import static com.kayhut.fuse.unipop.controller.utils.SearchAppenderUtil.*;

/**
 * Created by liorp on 4/2/2017.
 */
public class PromiseElementVertexController implements SearchQuery.SearchController {

    //region Constructors
    public PromiseElementVertexController(Client client, ElasticGraphConfiguration configuration, UniGraph graph, GraphElementSchemaProvider schemaProvider, MetricRegistry metricRegistry) {
        this.client = client;
        this.configuration = configuration;
        this.graph = graph;
        this.schemaProvider = schemaProvider;
        this.metricRegistry = metricRegistry;
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

        List<HasContainer> selectPHasContainers = Stream.ofAll(searchQuery.getPredicates().getPredicates())
                .filter(hasContainer -> hasContainer.getPredicate().getBiPredicate() != null)
                .filter(hasContainer -> hasContainer.getPredicate().getBiPredicate() instanceof SelectP)
                .toJavaList();

        return (Iterator<E>) (promiseHasContainers.isEmpty() ?
                queryPromiseVertices(searchQuery, constraintHasContainers, selectPHasContainers) :
                createPromiseVertices(searchQuery,promiseHasContainers, constraintHasContainers));
    }
    //endregion

    //region Private Methods

    /**
     * This method will create promise vertices from promise and constraint predicates.
     * The method need not actually query the data store for the vertices as it is a promise vertex with a promise predicate.
     *
     *
     * @param searchQuery
     * @param promiseHasContainers     - the promise predicate
     * @param constraintHasConstainers - the constraint predicate
     * @return the promise vertex iterator
     */
    private Iterator<Vertex> createPromiseVertices(SearchQuery searchQuery, List<HasContainer> promiseHasContainers, List<HasContainer> constraintHasConstainers) {
        List<Promise> promises = Collections.emptyList();
        BiPredicate promisePredicate = promiseHasContainers.get(0).getBiPredicate();

        if (promisePredicate.equals(Compare.eq)) {
            promises = Stream.of(promiseHasContainers.get(0).getValue()).map(p -> (Promise) p).toJavaList();
        } else if (promisePredicate.equals(Contains.within)) {
            promises = CollectionUtil.listFromObjectValue(promiseHasContainers.get(0).getValue());
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
     *
     * @param searchQuery
     * @param constraintHasContainers - the constraint predicate
     * @return the promise vertex iterator
     */
    private Iterator<Element> queryPromiseVertices(
            SearchQuery searchQuery,
            List<HasContainer> constraintHasContainers,
            List<HasContainer> selectPHasContainers) {
        Optional<TraversalConstraint> constraint = constraintHasContainers.stream()
                .findFirst().filter(hasContainer -> hasContainer.getKey().toLowerCase().equals(GlobalConstants.HasKeys.CONSTRAINT))
                .map(h -> (TraversalConstraint) h.getValue());

        SearchBuilder searchBuilder = new SearchBuilder();
        PromiseElementControllerContext context = new PromiseElementControllerContext(
                this.graph,
                Collections.emptyList(),
                constraint,
                selectPHasContainers,
                this.schemaProvider,
                ElementType.vertex,
                searchQuery);

        //search appender
        CompositeSearchAppender<PromiseElementControllerContext> searchAppender = new CompositeSearchAppender<>(CompositeSearchAppender.Mode.all,
                wrap(new IndexSearchAppender()),
                wrap(new SizeSearchAppender(this.configuration)),
                wrap(new ElementConstraintSearchAppender()),
                wrap(new ElementGlobalTypeSearchAppender()),
                wrap(new FilterSourceSearchAppender()));

        searchAppender.append(searchBuilder, context);

        //compose
        SearchRequestBuilder searchRequest = searchBuilder.compose(client, false);
        SearchHitScrollIterable searchHits = new SearchHitScrollIterable(
                metricRegistry, client,
                searchRequest,
                searchBuilder.getLimit(),
                searchBuilder.getScrollSize(),
                searchBuilder.getScrollTime());

        return convert(searchHits, new SearchHitPromiseVertexConverter(graph));
    }

    private Iterator<Element> convert(Iterable<SearchHit> searchHitIterable, ElementConverter<SearchHit, Element> searchHitPromiseVertexConverter) {
        return Stream.ofAll(searchHitIterable)
                .map(searchHitPromiseVertexConverter::convert)
                .filter(Objects::nonNull).iterator();
    }

    //endregion

    private Client client;
    private ElasticGraphConfiguration configuration;
    //region Fields
    private UniGraph graph;
    private GraphElementSchemaProvider schemaProvider;
    private MetricRegistry metricRegistry;
    //endregion
}
