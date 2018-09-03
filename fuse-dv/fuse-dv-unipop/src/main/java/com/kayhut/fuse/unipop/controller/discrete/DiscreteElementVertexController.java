package com.kayhut.fuse.unipop.controller.discrete;

import com.kayhut.fuse.unipop.controller.ElasticGraphConfiguration;
import com.kayhut.fuse.unipop.controller.common.appender.*;
import com.kayhut.fuse.unipop.controller.common.context.CompositeControllerContext;
import com.kayhut.fuse.unipop.controller.common.converter.ElementConverter;
import com.kayhut.fuse.unipop.controller.discrete.context.DiscreteElementControllerContext;
import com.kayhut.fuse.unipop.controller.discrete.converter.DiscreteVertexConverter;
import com.kayhut.fuse.unipop.controller.promise.GlobalConstants;
import com.kayhut.fuse.unipop.controller.promise.appender.SizeSearchAppender;
import com.kayhut.fuse.unipop.controller.search.SearchBuilder;
import com.kayhut.fuse.unipop.controller.search.SearchOrderProviderFactory;
import com.kayhut.fuse.unipop.converter.SearchHitScrollIterable;
import com.kayhut.fuse.unipop.predicates.SelectP;
import com.kayhut.fuse.unipop.promise.TraversalConstraint;
import com.kayhut.fuse.unipop.schemaProviders.GraphElementSchemaProvider;
import com.kayhut.fuse.unipop.structure.ElementType;
import javaslang.collection.Stream;
import org.apache.tinkerpop.gremlin.process.traversal.Compare;
import org.apache.tinkerpop.gremlin.process.traversal.step.util.HasContainer;
import org.apache.tinkerpop.gremlin.structure.Element;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.client.Client;
import org.elasticsearch.search.SearchHit;
import org.unipop.process.Profiler;
import org.unipop.query.search.SearchQuery;
import org.unipop.structure.UniGraph;

import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static com.kayhut.fuse.unipop.controller.utils.SearchAppenderUtil.wrap;

/**
 * Created by roman.margolis on 12/09/2017.
 */
public class DiscreteElementVertexController implements SearchQuery.SearchController {
    //region Constructors
    public DiscreteElementVertexController(
            Client client,
            ElasticGraphConfiguration configuration,
            UniGraph graph,
            GraphElementSchemaProvider schemaProvider,
            SearchOrderProviderFactory orderProviderFactory) {

        this.client = client;
        this.configuration = configuration;
        this.graph = graph;
        this.orderProviderFactory = orderProviderFactory;
        this.schemaProvider = schemaProvider;
    }
    //endregion

    //region SearchController Implementation
    @Override
    public <E extends Element> Iterator<E> search(SearchQuery<E> searchQuery) {
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

        Optional<TraversalConstraint> constraint = constraintHasContainers.isEmpty() ?
                Optional.empty() :
                Optional.of((TraversalConstraint) constraintHasContainers.get(0).getValue());

        CompositeControllerContext context = new CompositeControllerContext.Impl(
                    new DiscreteElementControllerContext(this.graph,
                            ElementType.vertex,
                            this.schemaProvider,
                            constraint,
                            selectPHasContainers,
                            searchQuery.getLimit()),
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
        SearchHitScrollIterable searchHits = new SearchHitScrollIterable(
                client,
                searchRequest,
                orderProviderFactory.build(context),
                searchBuilder.getLimit(),
                searchBuilder.getScrollSize(), searchBuilder.getScrollTime());

        ElementConverter<SearchHit, E> elementConverter = new DiscreteVertexConverter<>(context);

        return Stream.ofAll(searchHits)
                .flatMap(elementConverter::convert)
                .filter(Objects::nonNull).iterator();
    }

    @Override
    public Profiler getProfiler() {
        return this.profiler;
    }

    @Override
    public void setProfiler(Profiler profiler) {
        this.profiler = profiler;
    }
    //endregion

    //region Fields
    private Client client;
    private ElasticGraphConfiguration configuration;
    private UniGraph graph;
    private SearchOrderProviderFactory orderProviderFactory;
    private GraphElementSchemaProvider schemaProvider;

    private Profiler profiler;
    //endregion
}
