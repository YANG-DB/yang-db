package com.kayhut.fuse.epb.plan.statistics.provider;

import com.codahale.metrics.MetricRegistry;
import com.google.inject.Inject;
import com.kayhut.fuse.epb.plan.statistics.configuration.StatConfig;
import com.kayhut.fuse.unipop.controller.search.SearchBuilder;
import com.kayhut.fuse.unipop.controller.utils.map.MapBuilder;
import com.kayhut.fuse.unipop.converter.SearchHitScrollIterable;
import javaslang.collection.Stream;
import org.elasticsearch.client.Client;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;

import java.util.Arrays;
import java.util.Map;

/**
 * Created by Roman on 21/06/2017.
 */
public class ElasticStatDocumentProvider implements StatDataProvider {
    //region Constructors
    @Inject
    public ElasticStatDocumentProvider(MetricRegistry metricRegistry, Client client, StatConfig config) {
        this.metricRegistry = metricRegistry;
        this.client = client;
        this.config = config;
    }
    //endregion

    //region StatDataProvider Implementation
    @Override
    public Iterable<Map<String, Object>> getStatDataItems(
            Iterable<String> indices,
            Iterable<String> types,
            Iterable<String> fields,
            Map<String, Object> constraints) {
        SearchBuilder searchBuilder = new SearchBuilder();
        searchBuilder.getIndices().add(config.getStatIndexName());
        searchBuilder.getIncludeSourceFields().add("*");
        searchBuilder.getQueryBuilder().query().filtered().filter().bool().push()
                .must().terms("index", indices).pop().push()
                .must().terms("type", types).pop().push()
                .must().terms("field", fields).pop().push();

        constraints.forEach((key, value) -> searchBuilder.getQueryBuilder().must().term(key, value).pop().push());

        searchBuilder.setLimit(Integer.MAX_VALUE);
        searchBuilder.setScrollSize(1000);
        searchBuilder.setScrollTime(60000);

        SearchHitScrollIterable hits = new SearchHitScrollIterable(
                metricRegistry,
                this.client,
                searchBuilder.compose(this.client, false),
                searchBuilder.getLimit(),
                searchBuilder.getScrollSize(),
                searchBuilder.getScrollTime());

        return Stream.ofAll(hits)
                .map(hit -> new MapBuilder<>(hit.sourceAsMap()).put("_type", hit.getType()).get())
                .toJavaList();
    }
    //endregion

    private MetricRegistry metricRegistry;
    //region Fields
    private Client client;
    private StatConfig config;
    //endregion
}
