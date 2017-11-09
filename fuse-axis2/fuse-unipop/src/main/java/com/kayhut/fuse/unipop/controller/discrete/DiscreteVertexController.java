package com.kayhut.fuse.unipop.controller.discrete;

import com.codahale.metrics.MetricRegistry;
import com.kayhut.fuse.unipop.controller.ElasticGraphConfiguration;
import com.kayhut.fuse.unipop.controller.common.VertexControllerBase;
import com.kayhut.fuse.unipop.controller.common.appender.*;
import com.kayhut.fuse.unipop.controller.common.converter.CompositeElementConverter;
import com.kayhut.fuse.unipop.controller.discrete.context.DiscreteVertexControllerContext;
import com.kayhut.fuse.unipop.controller.discrete.converter.DiscreteEdgeConverter;
import com.kayhut.fuse.unipop.controller.promise.appender.SizeSearchAppender;
import com.kayhut.fuse.unipop.controller.search.SearchBuilder;
import com.kayhut.fuse.unipop.controller.common.converter.ElementConverter;
import com.kayhut.fuse.unipop.converter.SearchHitScrollIterable;
import com.kayhut.fuse.unipop.predicates.SelectP;
import com.kayhut.fuse.unipop.promise.TraversalConstraint;
import com.kayhut.fuse.unipop.schemaProviders.GraphElementSchemaProvider;
import javaslang.collection.Stream;
import org.apache.tinkerpop.gremlin.process.traversal.Traversal;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__;
import org.apache.tinkerpop.gremlin.process.traversal.step.util.HasContainer;
import org.apache.tinkerpop.gremlin.structure.Edge;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.client.Client;
import org.elasticsearch.search.SearchHit;
import org.unipop.query.search.SearchVertexQuery;
import org.unipop.structure.UniGraph;

import java.util.*;

import static com.kayhut.fuse.unipop.controller.utils.SearchAppenderUtil.wrap;

/**
 * Created by roman.margolis on 13/09/2017.
 */
public class DiscreteVertexController extends VertexControllerBase {
    //region Constructors
    public DiscreteVertexController(Client client, ElasticGraphConfiguration configuration, UniGraph graph, GraphElementSchemaProvider schemaProvider, MetricRegistry metricRegistry) {
        super(Stream.ofAll(schemaProvider.getEdgeLabels()).distinct().toJavaList());
        this.client = client;
        this.configuration = configuration;
        this.graph = graph;
        this.schemaProvider = schemaProvider;
        this.metricRegistry = metricRegistry;
    }
    //endregion

    //region VertexControllerBase Implementation
    @Override
    protected Iterator<Edge> search(SearchVertexQuery searchVertexQuery, Iterable<String> edgeLabels) {
        if (Stream.ofAll(edgeLabels).isEmpty()) {
            return Collections.emptyIterator();
        }

        List<HasContainer> selectPHasContainers = Stream.ofAll(searchVertexQuery.getPredicates().getPredicates())
                .filter(hasContainer -> hasContainer.getPredicate().getBiPredicate() != null)
                .filter(hasContainer -> hasContainer.getPredicate().getBiPredicate() instanceof SelectP)
                .toJavaList();

        Traversal[] hasTraversals = Stream.ofAll(searchVertexQuery.getPredicates().getPredicates())
                .filter(hasContainer -> !selectPHasContainers.contains(hasContainer))
                .map(hasContainer -> (Traversal) __.has(hasContainer.getKey(), hasContainer.getPredicate()))
                .toJavaArray(Traversal.class);

        TraversalConstraint constraint = hasTraversals.length > 0 ?
                new TraversalConstraint(__.and(hasTraversals)) :
                TraversalConstraint.EMPTY ;

        DiscreteVertexControllerContext context = new DiscreteVertexControllerContext(
                this.graph,
                this.schemaProvider,
                constraint.equals(TraversalConstraint.EMPTY) ? Optional.empty() : Optional.of(constraint),
                selectPHasContainers,
                searchVertexQuery.getLimit(),
                searchVertexQuery.getDirection(),
                searchVertexQuery.getVertices());

        CompositeSearchAppender<DiscreteVertexControllerContext> searchAppender =
                new CompositeSearchAppender<>(CompositeSearchAppender.Mode.all,
                        wrap(new IndexSearchAppender()),
                        wrap(new SizeSearchAppender(this.configuration)),
                        wrap(new ConstraintSearchAppender()),
                        wrap(new FilterSourceSearchAppender()),
                        wrap(new FilterSourceRoutingSearchAppender()),
                        wrap(new ElementRoutingSearchAppender()),
                        wrap(new EdgeBulkSearchAppender()),
                        wrap(new EdgeSourceSearchAppender()),
                        wrap(new EdgeRoutingSearchAppender()),
                        wrap(new EdgeIndexSearchAppender()),
                        wrap(new NormalizeRoutingSearchAppender(50)),
                        wrap(new NormalizeIndexSearchAppender(100)));

        SearchBuilder searchBuilder = new SearchBuilder();
        searchAppender.append(searchBuilder, context);

        SearchRequestBuilder searchRequest = searchBuilder.build(client, false);
        SearchHitScrollIterable searchHits = new SearchHitScrollIterable(
                metricRegistry, client,
                searchRequest,
                searchBuilder.getLimit(),
                searchBuilder.getScrollSize(),
                searchBuilder.getScrollTime());

        ElementConverter<SearchHit, Edge> elementConverter = new CompositeElementConverter<>(
                new DiscreteEdgeConverter<>(context));

        return Stream.ofAll(searchHits)
                .flatMap(elementConverter::convert)
                .filter(Objects::nonNull).iterator();
    }
    //endregion

    //region Fields
    private Client client;
    private ElasticGraphConfiguration configuration;
    private UniGraph graph;
    private GraphElementSchemaProvider schemaProvider;
    private MetricRegistry metricRegistry;
    //endregion
}
