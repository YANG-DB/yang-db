package com.kayhut.fuse.unipop.controller.discrete;

import com.codahale.metrics.MetricRegistry;
import com.kayhut.fuse.unipop.controller.ElasticGraphConfiguration;
import com.kayhut.fuse.unipop.controller.common.appender.ConstraintSearchAppender;
import com.kayhut.fuse.unipop.controller.common.appender.ElementGlobalTypeSearchAppender;
import com.kayhut.fuse.unipop.controller.common.appender.IndexSearchAppender;
import com.kayhut.fuse.unipop.controller.discrete.context.DiscreteElementControllerContext;
import com.kayhut.fuse.unipop.controller.common.appender.CompositeSearchAppender;
import com.kayhut.fuse.unipop.controller.search.SearchBuilder;
import com.kayhut.fuse.unipop.converter.ElementConverter;
import com.kayhut.fuse.unipop.converter.SearchHitScrollIterable;
import com.kayhut.fuse.unipop.converter.discrete.SearchHitDiscreteVertexConverter;
import com.kayhut.fuse.unipop.promise.TraversalConstraint;
import com.kayhut.fuse.unipop.schemaProviders.GraphElementSchemaProvider;
import com.kayhut.fuse.unipop.structure.ElementType;
import javaslang.collection.Stream;
import org.apache.tinkerpop.gremlin.process.traversal.Traversal;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__;
import org.apache.tinkerpop.gremlin.structure.Element;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.client.Client;
import org.elasticsearch.search.SearchHit;
import org.unipop.query.search.SearchQuery;
import org.unipop.structure.UniGraph;

import java.util.Iterator;
import java.util.Objects;
import java.util.Optional;

import static com.kayhut.fuse.unipop.controller.utils.SearchAppenderUtil.*;

/**
 * Created by roman.margolis on 12/09/2017.
 */
public class DiscreteElementVertexController implements SearchQuery.SearchController {
    //region Constructors
    DiscreteElementVertexController(Client client, ElasticGraphConfiguration configuration, UniGraph graph, GraphElementSchemaProvider schemaProvider, MetricRegistry metricRegistry) {
        this.client = client;
        this.configuration = configuration;
        this.graph = graph;
        this.schemaProvider = schemaProvider;
        this.metricRegistry = metricRegistry;
    }
    //endregion

    //region SearchController Implementation
    @Override
    public <E extends Element> Iterator<E> search(SearchQuery<E> searchQuery) {
        Traversal[] hasTraversals = Stream.ofAll(searchQuery.getPredicates().getPredicates())
                .map(hasContainer -> (Traversal)__.has(hasContainer.getKey(), hasContainer.getPredicate()))
                .toJavaArray(Traversal.class);

        TraversalConstraint constraint = hasTraversals.length > 0 ?
                new TraversalConstraint(__.and(hasTraversals)) :
                TraversalConstraint.EMPTY ;

        DiscreteElementControllerContext context = new DiscreteElementControllerContext(
                constraint.equals(TraversalConstraint.EMPTY) ? Optional.empty() : Optional.of(constraint),
                ElementType.vertex,
                this.schemaProvider);

        CompositeSearchAppender<DiscreteElementControllerContext> searchAppender =
                new CompositeSearchAppender<>(CompositeSearchAppender.Mode.all,
                        wrap(new IndexSearchAppender()),
                        wrap(new ElementGlobalTypeSearchAppender()),
                        wrap(new ConstraintSearchAppender()));

        SearchBuilder searchBuilder = new SearchBuilder();
        searchAppender.append(searchBuilder, context);

        SearchRequestBuilder searchRequest = searchBuilder.compose(client, false);
        SearchHitScrollIterable searchHits = new SearchHitScrollIterable(
                metricRegistry, client,
                searchRequest,
                searchBuilder.getLimit(),
                searchBuilder.getScrollSize(),
                searchBuilder.getScrollTime());

        ElementConverter<SearchHit, E> elementConverter = new SearchHitDiscreteVertexConverter<>(this.graph);
        return Stream.ofAll(searchHits)
                .map(elementConverter::convert)
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
