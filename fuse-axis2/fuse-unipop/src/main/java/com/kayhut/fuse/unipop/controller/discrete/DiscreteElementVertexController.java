package com.kayhut.fuse.unipop.controller.discrete;

import com.codahale.metrics.MetricRegistry;
import com.kayhut.fuse.unipop.controller.ElasticGraphConfiguration;
import com.kayhut.fuse.unipop.controller.common.appender.ElementGlobalTypeSearchAppender;
import com.kayhut.fuse.unipop.controller.common.appender.IndexSearchAppender;
import com.kayhut.fuse.unipop.controller.discrete.context.DiscreteElementControllerContext;
import com.kayhut.fuse.unipop.controller.promise.appender.CompositeSearchAppender;
import com.kayhut.fuse.unipop.controller.utils.SearchAppenderUtil;
import com.kayhut.fuse.unipop.promise.TraversalConstraint;
import com.kayhut.fuse.unipop.schemaProviders.GraphElementSchemaProvider;
import javaslang.collection.Stream;
import org.apache.tinkerpop.gremlin.process.traversal.Traversal;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__;
import org.apache.tinkerpop.gremlin.structure.Element;
import org.elasticsearch.client.Client;
import org.unipop.query.search.SearchQuery;
import org.unipop.structure.UniGraph;

import java.util.Iterator;
import java.util.Optional;

import static com.kayhut.fuse.unipop.controller.utils.SearchAppenderUtil.*;

/**
 * Created by roman.margolis on 12/09/2017.
 */
public class DiscreteElementVertexController implements SearchQuery.SearchController{
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
                constraint.equals(TraversalConstraint.EMPTY) ? Optional.empty() : Optional.of(constraint));

        CompositeSearchAppender<DiscreteElementControllerContext> searchAppender =
                new CompositeSearchAppender<DiscreteElementControllerContext>(CompositeSearchAppender.Mode.all,
                        wrap(new IndexSearchAppender()),
                        wrap(new ElementGlobalTypeSearchAppender()));

        return null;
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
