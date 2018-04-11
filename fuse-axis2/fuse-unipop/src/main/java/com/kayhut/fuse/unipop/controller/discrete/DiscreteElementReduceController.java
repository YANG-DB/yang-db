package com.kayhut.fuse.unipop.controller.discrete;

import com.kayhut.fuse.unipop.controller.ElasticGraphConfiguration;
import com.kayhut.fuse.unipop.controller.common.appender.*;
import com.kayhut.fuse.unipop.controller.common.context.CompositeControllerContext;
import com.kayhut.fuse.unipop.controller.discrete.context.DiscreteElementControllerContext;
import com.kayhut.fuse.unipop.controller.promise.GlobalConstants;
import com.kayhut.fuse.unipop.controller.promise.appender.SizeSearchAppender;
import com.kayhut.fuse.unipop.controller.search.SearchBuilder;
import com.kayhut.fuse.unipop.predicates.SelectP;
import com.kayhut.fuse.unipop.promise.TraversalConstraint;
import com.kayhut.fuse.unipop.schemaProviders.GraphElementSchemaProvider;
import com.kayhut.fuse.unipop.structure.ElementType;
import javaslang.collection.Stream;
import org.apache.tinkerpop.gremlin.process.traversal.Compare;
import org.apache.tinkerpop.gremlin.process.traversal.step.util.HasContainer;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Client;
import org.unipop.query.aggregation.ReduceQuery;
import org.unipop.structure.UniGraph;

import java.util.List;
import java.util.Optional;

import static com.kayhut.fuse.unipop.controller.utils.SearchAppenderUtil.wrap;

/**
 * Created by Roman on 3/14/2018.
 */
public class DiscreteElementReduceController implements ReduceQuery.SearchController {
    //region Constructors
    public DiscreteElementReduceController(
            Client client,
            ElasticGraphConfiguration configuration,
            UniGraph graph,
            GraphElementSchemaProvider schemaProvider) {

        this.client = client;
        this.configuration = configuration;
        this.graph = graph;
        this.schemaProvider = schemaProvider;
    }
    //endregion

    //region ReduceQuery.SearchController Implementation
    @Override
    public long count(ReduceQuery reduceQuery) {
        List<HasContainer> constraintHasContainers = Stream.ofAll(reduceQuery.getPredicates().getPredicates())
                .filter(hasContainer -> hasContainer.getKey().toLowerCase().equals(GlobalConstants.HasKeys.CONSTRAINT))
                .toJavaList();
        if (constraintHasContainers.size() > 1 ||
                (!constraintHasContainers.isEmpty() && !constraintHasContainers.get(0).getBiPredicate().equals(Compare.eq))) {
            throw new UnsupportedOperationException("Single \"" + GlobalConstants.HasKeys.CONSTRAINT + "\" allowed");
        }

        List<HasContainer> selectPHasContainers = Stream.ofAll(reduceQuery.getPredicates().getPredicates())
                .filter(hasContainer -> hasContainer.getPredicate().getBiPredicate() != null)
                .filter(hasContainer -> hasContainer.getPredicate().getBiPredicate() instanceof SelectP)
                .toJavaList();

        Optional<TraversalConstraint> constraint = constraintHasContainers.isEmpty() ?
                Optional.empty() :
                Optional.of((TraversalConstraint) constraintHasContainers.get(0).getValue());

        CompositeControllerContext context = new CompositeControllerContext.Impl(
                new DiscreteElementControllerContext(this.graph,
                        ElementType.vertex,
                        this.schemaProvider,
                        constraint,
                        selectPHasContainers,
                        0),
                null);

        CompositeSearchAppender<CompositeControllerContext> searchAppender =
                new CompositeSearchAppender<>(CompositeSearchAppender.Mode.all,
                        wrap(new ElementIndexSearchAppender()),
                        wrap(new SizeSearchAppender(this.configuration)),
                        wrap(new ConstraintSearchAppender()),
                        wrap(new FilterSourceSearchAppender()),
                        wrap(new FilterSourceRoutingSearchAppender()),
                        wrap(new ElementRoutingSearchAppender()),
                        wrap(new MustFetchSourceSearchAppender("type")),
                        wrap(new NormalizeRoutingSearchAppender(50)),
                        wrap(new NormalizeIndexSearchAppender(100)));

        SearchBuilder searchBuilder = new SearchBuilder();
        searchAppender.append(searchBuilder, context);

        SearchRequestBuilder searchRequest = searchBuilder.build(client, false);
        SearchResponse response = searchRequest.execute().actionGet();
        return response.getHits().getTotalHits();
    }
    //endregion

    //region Fields
    private Client client;
    private ElasticGraphConfiguration configuration;
    private UniGraph graph;
    private GraphElementSchemaProvider schemaProvider;
    //endregion
}
