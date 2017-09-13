package com.kayhut.fuse.unipop.controller.discrete;

import com.codahale.metrics.MetricRegistry;
import com.kayhut.fuse.unipop.controller.ElasticGraphConfiguration;
import com.kayhut.fuse.unipop.controller.common.VertexControllerBase;
import com.kayhut.fuse.unipop.controller.common.appender.CompositeSearchAppender;
import com.kayhut.fuse.unipop.controller.common.appender.ConstraintSearchAppender;
import com.kayhut.fuse.unipop.controller.common.appender.ElementGlobalTypeSearchAppender;
import com.kayhut.fuse.unipop.controller.common.appender.IndexSearchAppender;
import com.kayhut.fuse.unipop.controller.discrete.appender.SingularEdgeAppender;
import com.kayhut.fuse.unipop.controller.discrete.context.DiscreteElementControllerContext;
import com.kayhut.fuse.unipop.controller.discrete.context.DiscreteVertexControllerContext;
import com.kayhut.fuse.unipop.promise.TraversalConstraint;
import com.kayhut.fuse.unipop.schemaProviders.GraphElementSchemaProvider;
import com.kayhut.fuse.unipop.structure.ElementType;
import javaslang.collection.Stream;
import org.apache.tinkerpop.gremlin.process.traversal.Traversal;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__;
import org.apache.tinkerpop.gremlin.structure.Edge;
import org.elasticsearch.client.Client;
import org.unipop.query.search.SearchVertexQuery;
import org.unipop.structure.UniGraph;

import java.util.Collections;
import java.util.Iterator;
import java.util.Optional;

import static com.kayhut.fuse.unipop.controller.utils.SearchAppenderUtil.wrap;

/**
 * Created by roman.margolis on 13/09/2017.
 */
public class DiscreteVertexController extends VertexControllerBase {
    //region Constructors
    DiscreteVertexController(Client client, ElasticGraphConfiguration configuration, UniGraph graph, GraphElementSchemaProvider schemaProvider, MetricRegistry metricRegistry) {
        super(Stream.ofAll(schemaProvider.getEdgeTypes()).distinct().toJavaList());
    }
    //endregion

    //region VertexControllerBase Implementation
    @Override
    protected Iterator<Edge> search(SearchVertexQuery searchVertexQuery, Iterable<String> edgeLabels) {
        if (Stream.ofAll(edgeLabels).isEmpty()) {
            return Collections.emptyIterator();
        }

        Traversal[] hasTraversals = Stream.ofAll(searchVertexQuery.getPredicates().getPredicates())
                .map(hasContainer -> (Traversal) __.has(hasContainer.getKey(), hasContainer.getPredicate()))
                .toJavaArray(Traversal.class);

        TraversalConstraint constraint = hasTraversals.length > 0 ?
                new TraversalConstraint(__.and(hasTraversals)) :
                TraversalConstraint.EMPTY ;

        DiscreteVertexControllerContext context = new DiscreteVertexControllerContext(
                searchVertexQuery.getVertices(),
                searchVertexQuery.getDirection(),
                constraint.equals(TraversalConstraint.EMPTY) ? Optional.empty() : Optional.of(constraint),
                ElementType.edge,
                this.schemaProvider);

        CompositeSearchAppender<DiscreteVertexControllerContext> searchAppender =
                new CompositeSearchAppender<>(CompositeSearchAppender.Mode.all,
                        wrap(new IndexSearchAppender()),
                        wrap(new ElementGlobalTypeSearchAppender()),
                        wrap(new ConstraintSearchAppender()),
                        wrap(new SingularEdgeAppender()));

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
